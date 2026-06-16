package cz.vanama.courtflow.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import cz.vanama.courtflow.core.network.generated.model.NbaV1TeamsGet200Response
import cz.vanama.courtflow.core.network.generated.model.NbaV1TeamsIdGet200Response
import cz.vanama.courtflow.data.cache.CachePolicy
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TeamRepositoryImplTest {
    private lateinit var database: CourtFlowDatabase
    private lateinit var api: NBAApi
    private lateinit var repository: TeamRepositoryImpl
    private var now = 0L

    @Before
    fun setup() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    CourtFlowDatabase::class.java,
                ).build()
        api = mockk()
        repository = TeamRepositoryImpl(api, database.teamDao(), database.cacheMetadataDao(), nowMillis = { now })
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getTeams emits mapped teams`() =
        runTest {
            coEvery { api.nbaV1TeamsGet() } returns NbaV1TeamsGet200Response(data = listOf(atlantaDto))

            repository.getTeams().test {
                val result = awaitItem()
                assertEquals(1, result.size)
                assertEquals("Atlanta Hawks", result[0].fullName)
                awaitComplete()
            }
        }

    @Test
    fun `getTeamById returns mapped team`() =
        runTest {
            coEvery { api.nbaV1TeamsIdGet(1) } returns NbaV1TeamsIdGet200Response(atlantaDto)

            val result = repository.getTeamById(1)

            assertEquals("Atlanta Hawks", result.fullName)
        }

    @Test
    fun `getTeams hits the network only once and serves the cache afterwards`() =
        runTest {
            coEvery { api.nbaV1TeamsGet() } returns NbaV1TeamsGet200Response(data = listOf(atlantaDto))

            repository.getTeams().test {
                awaitItem()
                awaitComplete()
            }
            repository.getTeams().test {
                assertEquals("Atlanta Hawks", awaitItem()[0].fullName)
                awaitComplete()
            }

            coVerify(exactly = 1) { api.nbaV1TeamsGet() }
        }

    @Test
    fun `getTeamById serves a cached team without a network call`() =
        runTest {
            coEvery { api.nbaV1TeamsGet() } returns NbaV1TeamsGet200Response(data = listOf(atlantaDto))

            repository.getTeams().test {
                awaitItem()
                awaitComplete()
            }
            val result = repository.getTeamById(1)

            assertEquals("Atlanta Hawks", result.fullName)
            coVerify(exactly = 0) { api.nbaV1TeamsIdGet(any()) }
        }

    @Test
    fun `team cache survives a new repository instance`() =
        runTest {
            coEvery { api.nbaV1TeamsGet() } returns NbaV1TeamsGet200Response(data = listOf(atlantaDto))
            repository.getTeams().test {
                awaitItem()
                awaitComplete()
            }

            // No stubs on this mock: any network call would fail the test.
            val offlineApi = mockk<NBAApi>()
            val freshRepository =
                TeamRepositoryImpl(offlineApi, database.teamDao(), database.cacheMetadataDao(), nowMillis = { now })

            freshRepository.getTeams().test {
                assertEquals("Atlanta Hawks", awaitItem()[0].fullName)
                awaitComplete()
            }
            assertEquals("Atlanta Hawks", freshRepository.getTeamById(1).fullName)
        }

    @Test
    fun `getTeams translates a malformed team payload into a DataException`() =
        runTest {
            coEvery { api.nbaV1TeamsGet() } returns
                NbaV1TeamsGet200Response(data = listOf(atlantaDto.copy(id = null)))

            repository.getTeams().test {
                val error = awaitError()
                assertTrue("expected DataException, was $error", error is DataException)
                assertEquals(DataErrorKind.UNKNOWN, (error as DataException).kind)
            }
        }

    @Test
    fun `getTeams does not cache a failed fetch and recovers on retry`() =
        runTest {
            coEvery { api.nbaV1TeamsGet() } returns
                NbaV1TeamsGet200Response(data = listOf(atlantaDto.copy(id = null))) andThen
                NbaV1TeamsGet200Response(data = listOf(atlantaDto))

            repository.getTeams().test { awaitError() }
            repository.getTeams().test {
                assertEquals("Atlanta Hawks", awaitItem()[0].fullName)
                awaitComplete()
            }

            coVerify(exactly = 2) { api.nbaV1TeamsGet() }
        }

    @Test
    fun `getTeams refetches once the cache is older than the TTL`() =
        runTest {
            coEvery { api.nbaV1TeamsGet() } returns NbaV1TeamsGet200Response(data = listOf(atlantaDto))
            repository.getTeams().test {
                awaitItem()
                awaitComplete()
            }

            now = CachePolicy.TTL.inWholeMilliseconds
            repository.getTeams().test {
                assertEquals("Atlanta Hawks", awaitItem()[0].fullName)
                awaitComplete()
            }

            coVerify(exactly = 2) { api.nbaV1TeamsGet() }
        }

    @Test
    fun `getTeams serves the cache without a network call while fresh`() =
        runTest {
            coEvery { api.nbaV1TeamsGet() } returns NbaV1TeamsGet200Response(data = listOf(atlantaDto))
            repository.getTeams().test {
                awaitItem()
                awaitComplete()
            }

            now = CachePolicy.TTL.inWholeMilliseconds - 1
            repository.getTeams().test {
                assertEquals("Atlanta Hawks", awaitItem()[0].fullName)
                awaitComplete()
            }

            coVerify(exactly = 1) { api.nbaV1TeamsGet() }
        }

    @Test
    fun `getTeams serves stale cache when a stale refresh fails`() =
        runTest {
            coEvery { api.nbaV1TeamsGet() } returns
                NbaV1TeamsGet200Response(data = listOf(atlantaDto)) andThenThrows IOException("offline")
            repository.getTeams().test {
                awaitItem()
                awaitComplete()
            }

            now = CachePolicy.TTL.inWholeMilliseconds
            repository.getTeams().test {
                assertEquals("Atlanta Hawks", awaitItem()[0].fullName)
                awaitComplete()
            }

            coVerify(exactly = 2) { api.nbaV1TeamsGet() }
        }

    @Test
    fun `forceRefresh refetches even when the cache is fresh`() =
        runTest {
            coEvery { api.nbaV1TeamsGet() } returns NbaV1TeamsGet200Response(data = listOf(atlantaDto))
            repository.getTeams().test {
                awaitItem()
                awaitComplete()
            }

            repository.getTeams(forceRefresh = true).test {
                assertEquals("Atlanta Hawks", awaitItem()[0].fullName)
                awaitComplete()
            }

            coVerify(exactly = 2) { api.nbaV1TeamsGet() }
        }

    private val atlantaDto =
        NBATeam(
            id = 1,
            abbreviation = "ATL",
            city = "Atlanta",
            conference = NBATeam.Conference.East,
            division = NBATeam.Division.Southeast,
            fullName = "Atlanta Hawks",
            name = "Hawks",
        )
}
