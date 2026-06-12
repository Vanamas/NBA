package cz.vanama.courtflow.data.repository

import app.cash.turbine.test
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import cz.vanama.courtflow.core.network.generated.model.NbaV1TeamsGet200Response
import cz.vanama.courtflow.core.network.generated.model.NbaV1TeamsIdGet200Response
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TeamRepositoryImplTest {
    private lateinit var api: NBAApi
    private lateinit var repository: TeamRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repository = TeamRepositoryImpl(api)
    }

    @Test
    fun `getTeams emits mapped teams`() =
        runTest {
            val teamDto =
                NBATeam(
                    id = 1,
                    abbreviation = "ATL",
                    city = "Atlanta",
                    conference = NBATeam.Conference.East,
                    division = NBATeam.Division.Southeast,
                    fullName = "Atlanta Hawks",
                    name = "Hawks",
                )
            val response = NbaV1TeamsGet200Response(data = listOf(teamDto))
            coEvery { api.nbaV1TeamsGet() } returns response

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
            val teamDto =
                NBATeam(
                    id = 1,
                    abbreviation = "ATL",
                    city = "Atlanta",
                    conference = NBATeam.Conference.East,
                    division = NBATeam.Division.Southeast,
                    fullName = "Atlanta Hawks",
                    name = "Hawks",
                )
            coEvery { api.nbaV1TeamsIdGet(1) } returns NbaV1TeamsIdGet200Response(teamDto)

            val result = repository.getTeamById(1)

            assertEquals("Atlanta Hawks", result.fullName)
        }

    @Test
    fun `getTeams hits the network only once and serves the cache afterwards`() =
        runTest {
            val response = NbaV1TeamsGet200Response(data = listOf(atlantaDto))
            coEvery { api.nbaV1TeamsGet() } returns response

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
            val response = NbaV1TeamsGet200Response(data = listOf(atlantaDto))
            coEvery { api.nbaV1TeamsGet() } returns response

            repository.getTeams().test {
                awaitItem()
                awaitComplete()
            }
            val result = repository.getTeamById(1)

            assertEquals("Atlanta Hawks", result.fullName)
            coVerify(exactly = 0) { api.nbaV1TeamsIdGet(any()) }
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
