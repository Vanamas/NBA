package cz.vanama.courtflow.data.repository

import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.core.network.generated.model.NBAPlayer
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import cz.vanama.courtflow.core.network.generated.model.NbaV1PlayersGet200Response
import cz.vanama.courtflow.core.network.generated.model.NbaV1PlayersIdGet200Response
import cz.vanama.courtflow.core.network.generated.model.Pagination
import cz.vanama.courtflow.data.cache.CachePolicy
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.domain.model.PlayerFilter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlayerRepositoryImplTest {
    /**
     * Shared by [runTest] and Room (`setQueryCoroutineContext`): with every
     * query, transaction and invalidation on the single virtual-time
     * dispatcher, the Pager + mediator + Room pipeline driven by [asSnapshot]
     * is fully deterministic instead of racing Room's real executors.
     */
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var database: CourtFlowDatabase
    private lateinit var api: NBAApi
    private lateinit var repository: PlayerRepositoryImpl
    private var now = 0L

    @Before
    fun setup() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    CourtFlowDatabase::class.java,
                ).setQueryCoroutineContext(testDispatcher)
                .allowMainThreadQueries()
                .build()
        api = mockk()
        repository = PlayerRepositoryImpl(api, database, nowMillis = { now })
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `team filter hits the network with team_ids and bypasses the cache`() =
        runTest(testDispatcher) {
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 35, search = null, teamIds = listOf(14))
            } returns
                NbaV1PlayersGet200Response(
                    data = listOf(lebronDto),
                    meta = Pagination(nextCursor = null),
                )

            val players = repository.getPlayers(PlayerFilter(teamId = 14)).asSnapshot()

            assertEquals(listOf("LeBron"), players.map { it.firstName })
            assertTrue(database.playerDao().getAll().isEmpty())
            coVerify(exactly = 1) {
                api.nbaV1PlayersGet(cursor = null, perPage = 35, search = null, teamIds = listOf(14))
            }
        }

    @Test
    fun `position filter keeps only matching players from the page`() =
        runTest(testDispatcher) {
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 35, search = null, teamIds = null)
            } returns
                NbaV1PlayersGet200Response(
                    // lebronDto.position = "F", curryDto.position = "G"
                    data = listOf(lebronDto, curryDto),
                    meta = Pagination(nextCursor = null),
                )

            val players = repository.getPlayers(PlayerFilter(position = "G")).asSnapshot()

            assertEquals(listOf("Stephen"), players.map { it.firstName })
        }

    @Test
    fun `combined team and position filter sends team_ids and filters position client-side`() =
        runTest(testDispatcher) {
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 35, search = null, teamIds = listOf(10))
            } returns
                NbaV1PlayersGet200Response(
                    data = listOf(curryDto, lebronDto),
                    meta = Pagination(nextCursor = null),
                )

            val players =
                repository.getPlayers(PlayerFilter(teamId = 10, position = "G")).asSnapshot()

            assertEquals(listOf("Stephen"), players.map { it.firstName })
        }

    @Test
    fun `empty filter is served offline-first from Room`() =
        runTest(testDispatcher) {
            coEvery { api.nbaV1PlayersGet(cursor = null, perPage = 35) } returns
                NbaV1PlayersGet200Response(
                    data = listOf(lebronDto),
                    meta = Pagination(nextCursor = null),
                )

            val players = repository.getPlayers(PlayerFilter()).asSnapshot()

            assertEquals(listOf("LeBron"), players.map { it.firstName })
            assertEquals(listOf(1), database.playerDao().getAll().map { it.id })
        }

    @Test
    fun `getPlayerById returns the mapped player`() =
        runTest(testDispatcher) {
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
    fun `unfiltered list is fetched once, cached in Room and mapped to domain`() =
        runTest(testDispatcher) {
            coEvery { api.nbaV1PlayersGet(cursor = null, perPage = 35) } returns
                NbaV1PlayersGet200Response(
                    data = listOf(lebronDto),
                    meta = Pagination(nextCursor = null),
                )

            val players = repository.getPlayers(query = null).asSnapshot()

            assertEquals(listOf("LeBron"), players.map { it.firstName })
            assertEquals(listOf(1), database.playerDao().getAll().map { it.id })
        }

    @Test
    fun `cached players are served when the network fails`() =
        runTest(testDispatcher) {
            coEvery { api.nbaV1PlayersGet(cursor = null, perPage = 35) } returns
                NbaV1PlayersGet200Response(
                    data = listOf(lebronDto),
                    meta = Pagination(nextCursor = null),
                )
            repository.getPlayers(query = null).asSnapshot()

            val offlineApi = mockk<NBAApi>()
            coEvery { offlineApi.nbaV1PlayersGet(cursor = null, perPage = 35) } throws IOException("offline")
            val offlineRepository = PlayerRepositoryImpl(offlineApi, database)

            val players =
                offlineRepository
                    .getPlayers(query = null)
                    .asSnapshot(
                        // RETRY is the only recovery that lets the Room source
                        // finish serving the cache: RETURN_CURRENT_SNAPSHOT
                        // aborts while the mediator error precedes the source
                        // load (and escapes paging-testing's internal refresh
                        // job once items are present). Each awaitNotLoading
                        // handles the error once, so the retries terminate.
                        onError = { ErrorRecovery.RETRY },
                    )

            assertEquals(listOf("LeBron"), players.map { it.firstName })
        }

    @Test
    fun `search query bypasses the cache and hits the network directly`() =
        runTest(testDispatcher) {
            coEvery { api.nbaV1PlayersGet(cursor = null, perPage = 35, search = "curry") } returns
                NbaV1PlayersGet200Response(
                    data = listOf(curryDto),
                    meta = Pagination(nextCursor = null),
                )

            val players = repository.getPlayers(query = "curry").asSnapshot()

            assertEquals(listOf("Stephen"), players.map { it.firstName })
            assertTrue(database.playerDao().getAll().isEmpty())
        }

    @Test
    fun `getPlayers requests exactly 35 players on the initial load`() =
        runTest(testDispatcher) {
            // nextCursor = null stops the mediator from appending further
            // pages, so the strict mock proves initialLoadSize == 35: the
            // default (3 x pageSize = 105) would match no stub and fail.
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 35)
            } returns playersResponse(offset = 0, nextCursor = null)

            val snapshot = repository.getPlayers(query = null).asSnapshot()

            assertEquals(35, snapshot.size)
            coVerify(exactly = 1) {
                api.nbaV1PlayersGet(cursor = null, perPage = 35)
            }
        }

    @Test
    fun `getPlayers requests exactly 35 players per appended page`() =
        runTest(testDispatcher) {
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 35)
            } returns playersResponse(offset = 0, nextCursor = 35)
            coEvery {
                api.nbaV1PlayersGet(cursor = 35, perPage = 35)
            } returns playersResponse(offset = 35, nextCursor = null)

            repository.getPlayers(query = null).asSnapshot {
                scrollTo(40)
            }

            // The mediator appended exactly one page of 35 from the stored
            // cursor; both API pages are cached afterwards.
            coVerify(exactly = 1) {
                api.nbaV1PlayersGet(cursor = 35, perPage = 35)
            }
            assertEquals(70, database.playerDao().getAll().size)
        }

    @Test
    fun `getTeamPlayers filters by team id with the same 35 page size`() =
        runTest(testDispatcher) {
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
    fun `getPlayerById translates a missing player payload into UNKNOWN`() =
        runTest(testDispatcher) {
            coEvery { api.nbaV1PlayersIdGet(19) } returns NbaV1PlayersIdGet200Response(data = null)

            val e = runCatching { repository.getPlayerById(19) }.exceptionOrNull()

            assertTrue("expected DataException, was $e", e is DataException)
            assertEquals(DataErrorKind.UNKNOWN, (e as DataException).kind)
        }

    @Test
    fun `getPlayerById translates HTTP 404 into NOT_FOUND`() =
        runTest(testDispatcher) { expectGetPlayerByIdKind(httpException(404), DataErrorKind.NOT_FOUND) }

    @Test
    fun `getPlayerById translates HTTP 500 into SERVER`() =
        runTest(testDispatcher) { expectGetPlayerByIdKind(httpException(500), DataErrorKind.SERVER) }

    @Test
    fun `getPlayerById translates IOException into NETWORK`() =
        runTest(testDispatcher) { expectGetPlayerByIdKind(IOException("offline"), DataErrorKind.NETWORK) }

    private suspend fun expectGetPlayerByIdKind(
        thrown: Exception,
        expected: DataErrorKind,
    ) {
        coEvery { api.nbaV1PlayersIdGet(19) } throws thrown

        val e = runCatching { repository.getPlayerById(19) }.exceptionOrNull()

        assertTrue("expected DataException, was $e", e is DataException)
        assertEquals(expected, (e as DataException).kind)
    }

    @Test
    fun `getPlayerById serves the cached player without a network call while fresh`() =
        runTest(testDispatcher) {
            coEvery { api.nbaV1PlayersIdGet(19) } returns NbaV1PlayersIdGet200Response(data = curryDto)
            repository.getPlayerById(19) // seeds the cache + stamp at now = 0

            now = CachePolicy.TTL.inWholeMilliseconds - 1
            val result = repository.getPlayerById(19)

            assertEquals("Stephen", result.firstName)
            coVerify(exactly = 1) { api.nbaV1PlayersIdGet(19) }
        }

    @Test
    fun `getPlayerById refetches once the cached player is stale`() =
        runTest(testDispatcher) {
            coEvery { api.nbaV1PlayersIdGet(19) } returns NbaV1PlayersIdGet200Response(data = curryDto)
            repository.getPlayerById(19)

            now = CachePolicy.TTL.inWholeMilliseconds
            repository.getPlayerById(19)

            coVerify(exactly = 2) { api.nbaV1PlayersIdGet(19) }
        }

    @Test
    fun `getPlayerById serves the cached player when a stale refresh fails`() =
        runTest(testDispatcher) {
            coEvery { api.nbaV1PlayersIdGet(19) } returns
                NbaV1PlayersIdGet200Response(data = curryDto) andThenThrows IOException("offline")
            repository.getPlayerById(19)

            now = CachePolicy.TTL.inWholeMilliseconds
            val result = repository.getPlayerById(19)

            assertEquals("Stephen", result.firstName)
            coVerify(exactly = 2) { api.nbaV1PlayersIdGet(19) }
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

    private val lakersDto =
        NBATeam(
            id = 14,
            abbreviation = "LAL",
            city = "Los Angeles",
            conference = NBATeam.Conference.West,
            division = NBATeam.Division.Pacific,
            fullName = "Los Angeles Lakers",
            name = "Lakers",
        )

    private val lebronDto =
        NBAPlayer(
            id = 1,
            firstName = "LeBron",
            lastName = "James",
            position = "F",
            team = lakersDto,
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
