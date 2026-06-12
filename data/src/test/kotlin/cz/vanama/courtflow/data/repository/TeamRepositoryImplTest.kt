package cz.vanama.courtflow.data.repository

import app.cash.turbine.test
import cz.vanama.courtflow.core.network.api.BallDontLieApi
import cz.vanama.courtflow.core.network.model.CommonResponse
import cz.vanama.courtflow.core.network.model.SingleResponse
import cz.vanama.courtflow.core.network.model.TeamDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TeamRepositoryImplTest {
    private lateinit var api: BallDontLieApi
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
                TeamDto(
                    id = 1,
                    abbreviation = "ATL",
                    city = "Atlanta",
                    conference = "East",
                    division = "Southeast",
                    fullName = "Atlanta Hawks",
                    name = "Hawks",
                )
            val response = CommonResponse(data = listOf(teamDto))
            coEvery { api.getTeams() } returns response

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
                TeamDto(
                    id = 1,
                    abbreviation = "ATL",
                    city = "Atlanta",
                    conference = "East",
                    division = "Southeast",
                    fullName = "Atlanta Hawks",
                    name = "Hawks",
                )
            coEvery { api.getTeam(1) } returns SingleResponse(teamDto)

            val result = repository.getTeamById(1)

            assertEquals("Atlanta Hawks", result.fullName)
        }

    @Test
    fun `getTeams hits the network only once and serves the cache afterwards`() =
        runTest {
            val response = CommonResponse(data = listOf(atlantaDto))
            coEvery { api.getTeams() } returns response

            repository.getTeams().test {
                awaitItem()
                awaitComplete()
            }
            repository.getTeams().test {
                assertEquals("Atlanta Hawks", awaitItem()[0].fullName)
                awaitComplete()
            }

            coVerify(exactly = 1) { api.getTeams() }
        }

    @Test
    fun `getTeamById serves a cached team without a network call`() =
        runTest {
            val response = CommonResponse(data = listOf(atlantaDto))
            coEvery { api.getTeams() } returns response

            repository.getTeams().test {
                awaitItem()
                awaitComplete()
            }
            val result = repository.getTeamById(1)

            assertEquals("Atlanta Hawks", result.fullName)
            coVerify(exactly = 0) { api.getTeam(any()) }
        }

    private val atlantaDto =
        TeamDto(
            id = 1,
            abbreviation = "ATL",
            city = "Atlanta",
            conference = "East",
            division = "Southeast",
            fullName = "Atlanta Hawks",
            name = "Hawks",
        )
}
