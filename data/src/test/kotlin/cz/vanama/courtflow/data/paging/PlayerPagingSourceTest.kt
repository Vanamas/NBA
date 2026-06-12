package cz.vanama.courtflow.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.squareup.moshi.JsonDataException
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.core.network.generated.model.NBAPlayer
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import cz.vanama.courtflow.core.network.generated.model.NbaV1PlayersGet200Response
import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.error.DataException
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerPagingSourceTest {
    private val api: NBAApi = mockk()
    private val source = PlayerPagingSource(api)

    private val teamDto =
        NBATeam(
            id = 1,
            abbreviation = "ATL",
            city = "Atlanta",
            conference = NBATeam.Conference.East,
            division = NBATeam.Division.Southeast,
            fullName = "Atlanta Hawks",
            name = "Hawks",
        )

    private val domainPlayer =
        Player(
            id = 237,
            firstName = "LeBron",
            lastName = "James",
            position = "F",
            team =
                Team(
                    id = 1,
                    abbreviation = "ATL",
                    city = "Atlanta",
                    conference = "East",
                    division = "Southeast",
                    fullName = "Atlanta Hawks",
                    name = "Hawks",
                ),
        )

    @Test
    fun `load clamps the requested page size to the API maximum of 100`() =
        runTest {
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 100, search = null)
            } returns NbaV1PlayersGet200Response(data = emptyList())

            val result = refresh(loadSize = 105)

            assertTrue(result is PagingSource.LoadResult.Page)
            coVerify(exactly = 1) { api.nbaV1PlayersGet(cursor = null, perPage = 100, search = null) }
        }

    @Test
    fun `load translates a mapper invariant violation into an UNKNOWN error`() =
        runTest {
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 35, search = null)
            } returns NbaV1PlayersGet200Response(data = listOf(NBAPlayer(id = null, team = teamDto)))

            assertEquals(DataErrorKind.UNKNOWN, errorKindOf(refresh()))
        }

    @Test
    fun `load translates JsonDataException into an UNKNOWN error`() =
        runTest {
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 35, search = null)
            } throws JsonDataException("Expected one of [East, West] but was Intl")

            assertEquals(DataErrorKind.UNKNOWN, errorKindOf(refresh()))
        }

    @Test
    fun `getRefreshKey returns null without an anchor position`() {
        assertNull(source.getRefreshKey(pagingStateWith(anchorPosition = null)))
    }

    @Test
    fun `getRefreshKey restarts from the first page instead of doing cursor arithmetic`() {
        val page =
            PagingSource.LoadResult.Page(
                data = listOf(domainPlayer),
                prevKey = null,
                nextKey = 100,
            )

        assertNull(source.getRefreshKey(pagingStateWith(anchorPosition = 0, pages = listOf(page))))
    }

    private suspend fun refresh(loadSize: Int = 35): PagingSource.LoadResult<Int, Player> =
        source.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = loadSize,
                placeholdersEnabled = false,
            ),
        )

    private fun errorKindOf(result: PagingSource.LoadResult<Int, Player>): DataErrorKind? =
        ((result as? PagingSource.LoadResult.Error)?.throwable as? DataException)?.kind

    private fun pagingStateWith(
        anchorPosition: Int?,
        pages: List<PagingSource.LoadResult.Page<Int, Player>> = emptyList(),
    ): PagingState<Int, Player> =
        PagingState(
            pages = pages,
            anchorPosition = anchorPosition,
            config = PagingConfig(pageSize = 35),
            leadingPlaceholderCount = 0,
        )
}
