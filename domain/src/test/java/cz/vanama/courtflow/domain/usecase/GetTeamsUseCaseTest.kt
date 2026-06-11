package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.repository.TeamRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTeamsUseCaseTest {
    private val teamRepository: TeamRepository = mockk()
    private val useCase = GetTeamsUseCase(teamRepository)

    @Test
    fun `invoke returns teams from repository`() =
        runTest {
            val teams = listOf(Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers"))
            every { teamRepository.getTeams() } returns flowOf(teams)

            var collected: List<Team>? = null
            useCase().collect { collected = it }

            assertEquals(teams, collected)
        }
}
