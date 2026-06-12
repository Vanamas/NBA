package cz.vanama.courtflow.data.repository

import androidx.paging.testing.asSnapshot
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.core.network.generated.model.NBAPlayer
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import cz.vanama.courtflow.core.network.generated.model.NbaV1PlayersGet200Response
import cz.vanama.courtflow.core.network.generated.model.NbaV1PlayersIdGet200Response
import cz.vanama.courtflow.core.network.generated.model.Pagination
import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.error.DataException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class PlayerRepositoryImplTest {
    private lateinit var api: NBAApi
    private lateinit var repository: PlayerRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repository = PlayerRepositoryImpl(api)
    }

    @Test
    fun `getPlayerById returns the mapped player`() =
        runTest {
            coEvery { api.nbaV1PlayersIdGet(19) } returns NbaV1PlayersIdGet200Response(data = curryDto)

            val result = repository.getPlayerById(19)

            assertEquals(19, result.id)
            assertEquals("Stephen", result.firstName)
            assertEquals("Curry", result.lastName)
            assertEquals("G", result.position)
            assertEquals("30", result.jerseyNumber)
            assertEquals("Golden State Warriors", result.team.fullName)
        }

    @Test
    fun `getPlayers requests exactly 35 players on the initial load`() =
        runTest {
            // nextCursor = null stops Paging from prefetching a second page,
            // so the strict mock proves initialLoadSize == 35: the default
            // (3 x pageSize = 105) would be clamped to perPage = 100 by the
            // paging source, match no stub, and fail this test.
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 35, search = null, teamIds = null)
            } returns playersResponse(offset = 0, nextCursor = null)

            val snapshot = repository.getPlayers(query = null).asSnapshot()

            assertEquals(35, snapshot.size)
            coVerify(exactly = 1) {
                api.nbaV1PlayersGet(cursor = null, perPage = 35, search = null, teamIds = null)
            }
        }

    @Test
    fun `getPlayers requests exactly 35 players per appended page`() =
        runTest {
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 35, search = null, teamIds = null)
            } returns playersResponse(offset = 0, nextCursor = 35)
            coEvery {
                api.nbaV1PlayersGet(cursor = 35, perPage = 35, search = null, teamIds = null)
            } returns playersResponse(offset = 35, nextCursor = null)

            val snapshot =
                repository.getPlayers(query = null).asSnapshot {
                    scrollTo(40)
                }

            assertEquals(70, snapshot.size)
            coVerify(exactly = 1) {
                api.nbaV1PlayersGet(cursor = 35, perPage = 35, search = null, teamIds = null)
            }
        }

    @Test
    fun `getTeamPlayers filters by team id with the same 35 page size`() =
        runTest {
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 35, search = null, teamIds = listOf(10))
            } returns playersResponse(offset = 0, nextCursor = null)

            val snapshot = repository.getTeamPlayers(teamId = 10).asSnapshot()

            assertEquals(35, snapshot.size)
            coVerify(exactly = 1) {
                api.nbaV1PlayersGet(cursor = null, perPage = 35, search = null, teamIds = listOf(10))
            }
        }

    @Test
    fun `getPlayerById translates a missing player payload into UNKNOWN`() {
        coEvery { api.nbaV1PlayersIdGet(19) } returns NbaV1PlayersIdGet200Response(data = null)

        val e =
            assertThrows(DataException::class.java) {
                runBlocking { repository.getPlayerById(19) }
            }

        assertEquals(DataErrorKind.UNKNOWN, e.kind)
    }

    @Test
    fun `getPlayerById translates HTTP 404 into NOT_FOUND`() {
        expectGetPlayerByIdKind(httpException(404), DataErrorKind.NOT_FOUND)
    }

    @Test
    fun `getPlayerById translates HTTP 500 into SERVER`() {
        expectGetPlayerByIdKind(httpException(500), DataErrorKind.SERVER)
    }

    @Test
    fun `getPlayerById translates IOException into NETWORK`() {
        expectGetPlayerByIdKind(IOException("offline"), DataErrorKind.NETWORK)
    }

    private fun expectGetPlayerByIdKind(
        thrown: Exception,
        expected: DataErrorKind,
    ) {
        coEvery { api.nbaV1PlayersIdGet(19) } throws thrown

        val e =
            assertThrows(DataException::class.java) {
                runBlocking { repository.getPlayerById(19) }
            }

        assertEquals(expected, e.kind)
    }

    private fun httpException(code: Int): HttpException = HttpException(Response.error<Any>(code, "".toResponseBody()))

    /** A full 35-item page, matching the page size the repository must request. */
    private fun playersResponse(
        offset: Int,
        nextCursor: Int?,
    ): NbaV1PlayersGet200Response =
        NbaV1PlayersGet200Response(
            data = List(35) { index -> NBAPlayer(id = offset + index, team = warriorsDto) },
            meta = Pagination(nextCursor = nextCursor),
        )

    private val warriorsDto =
        NBATeam(
            id = 10,
            abbreviation = "GSW",
            city = "Golden State",
            conference = NBATeam.Conference.West,
            division = NBATeam.Division.Pacific,
            fullName = "Golden State Warriors",
            name = "Warriors",
        )

    private val curryDto =
        NBAPlayer(
            id = 19,
            firstName = "Stephen",
            lastName = "Curry",
            position = "G",
            height = "6-2",
            weight = "185",
            jerseyNumber = "30",
            college = "Davidson",
            country = "USA",
            draftYear = 2009,
            draftRound = 1,
            draftNumber = 7,
            team = warriorsDto,
        )
}
