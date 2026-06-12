package cz.vanama.courtflow.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.core.network.generated.model.NBAPlayer
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import cz.vanama.courtflow.core.network.generated.model.NbaV1PlayersGet200Response
import cz.vanama.courtflow.core.network.generated.model.Pagination
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.data.local.entity.PlayerEntity
import cz.vanama.courtflow.data.local.entity.RemoteKeyEntity
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.data.mapper.toEntity
import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.error.DataException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlayerRemoteMediatorTest {
    private lateinit var database: CourtFlowDatabase
    private lateinit var api: NBAApi
    private lateinit var mediator: PlayerRemoteMediator

    @Before
    fun setup() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    CourtFlowDatabase::class.java,
                ).build()
        api = mockk()
        mediator = PlayerRemoteMediator(api, database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `refresh stores the first page and its next cursor`() =
        runTest {
            coEvery { api.nbaV1PlayersGet(cursor = null, perPage = 35) } returns
                NbaV1PlayersGet200Response(
                    data = listOf(playerDto(id = 1)),
                    meta = Pagination(nextCursor = 2),
                )

            val result = mediator.load(LoadType.REFRESH, emptyState())

            assertTrue(result is RemoteMediator.MediatorResult.Success)
            assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
            assertEquals(listOf(1), database.playerDao().getAll().map { it.id })
            assertEquals(2, database.remoteKeyDao().get()?.nextCursor)
        }

    @Test
    fun `refresh replaces previously cached players and the stored cursor`() =
        runTest {
            database.playerDao().insertAll(listOf(playerDto(id = 99).toDomain().toEntity()))
            database.remoteKeyDao().insert(RemoteKeyEntity(nextCursor = 100))
            coEvery { api.nbaV1PlayersGet(cursor = null, perPage = 35) } returns
                NbaV1PlayersGet200Response(
                    data = listOf(playerDto(id = 1)),
                    meta = Pagination(nextCursor = 2),
                )

            mediator.load(LoadType.REFRESH, emptyState())

            assertEquals(listOf(1), database.playerDao().getAll().map { it.id })
            assertEquals(2, database.remoteKeyDao().get()?.nextCursor)
        }

    @Test
    fun `refresh reports end of pagination when the API returns no next cursor`() =
        runTest {
            coEvery { api.nbaV1PlayersGet(cursor = null, perPage = 35) } returns
                NbaV1PlayersGet200Response(
                    data = listOf(playerDto(id = 1)),
                    meta = Pagination(nextCursor = null),
                )

            val result = mediator.load(LoadType.REFRESH, emptyState())

            assertTrue(result is RemoteMediator.MediatorResult.Success)
            assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        }

    @Test
    fun `refresh maps a network failure to a mediator error with a domain exception`() =
        runTest {
            coEvery { api.nbaV1PlayersGet(cursor = null, perPage = 35) } throws IOException("offline")

            val result = mediator.load(LoadType.REFRESH, emptyState())

            assertTrue(result is RemoteMediator.MediatorResult.Error)
            val throwable = (result as RemoteMediator.MediatorResult.Error).throwable
            assertTrue(throwable is DataException)
            assertEquals(DataErrorKind.NETWORK, (throwable as DataException).kind)
        }

    @Test
    fun `refresh maps an HTTP 429 to a rate-limited mediator error`() =
        runTest {
            coEvery { api.nbaV1PlayersGet(cursor = null, perPage = 35) } throws
                HttpException(Response.error<Any>(429, "rate limited".toResponseBody()))

            val result = mediator.load(LoadType.REFRESH, emptyState())

            assertTrue(result is RemoteMediator.MediatorResult.Error)
            val throwable = (result as RemoteMediator.MediatorResult.Error).throwable
            assertEquals(DataErrorKind.RATE_LIMITED, (throwable as DataException).kind)
        }

    @Test
    fun `append continues from the stored cursor and advances it`() =
        runTest {
            database.remoteKeyDao().insert(RemoteKeyEntity(nextCursor = 2))
            coEvery { api.nbaV1PlayersGet(cursor = 2, perPage = 35) } returns
                NbaV1PlayersGet200Response(
                    data = listOf(playerDto(id = 36)),
                    meta = Pagination(nextCursor = 3),
                )

            val result = mediator.load(LoadType.APPEND, emptyState())

            assertTrue(result is RemoteMediator.MediatorResult.Success)
            assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
            assertEquals(listOf(36), database.playerDao().getAll().map { it.id })
            assertEquals(3, database.remoteKeyDao().get()?.nextCursor)
        }

    @Test
    fun `append ends pagination without a network call when the stored cursor is null`() =
        runTest {
            database.remoteKeyDao().insert(RemoteKeyEntity(nextCursor = null))

            val result = mediator.load(LoadType.APPEND, emptyState())

            assertTrue(result is RemoteMediator.MediatorResult.Success)
            assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
            coVerify(exactly = 0) {
                api.nbaV1PlayersGet(any(), any(), any(), any(), any(), any(), any())
            }
        }

    @Test
    fun `prepend always ends pagination immediately`() =
        runTest {
            val result = mediator.load(LoadType.PREPEND, emptyState())

            assertTrue(result is RemoteMediator.MediatorResult.Success)
            assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
            coVerify(exactly = 0) {
                api.nbaV1PlayersGet(any(), any(), any(), any(), any(), any(), any())
            }
        }

    private fun emptyState() =
        PagingState<Int, PlayerEntity>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 35, initialLoadSize = 35, enablePlaceholders = false),
            leadingPlaceholderCount = 0,
        )

    private fun playerDto(id: Int) =
        NBAPlayer(
            id = id,
            firstName = "LeBron",
            lastName = "James",
            position = "F",
            team =
                NBATeam(
                    id = 14,
                    abbreviation = "LAL",
                    city = "Los Angeles",
                    conference = NBATeam.Conference.West,
                    division = NBATeam.Division.Pacific,
                    fullName = "Los Angeles Lakers",
                    name = "Lakers",
                ),
        )
}
