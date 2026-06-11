package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetTeamDetailUseCaseTest {

    private lateinit var teamRepository: TeamRepository
    private lateinit var useCase: GetTeamDetailUseCase

    @Before
    fun setup() {
        teamRepository = mockk()
        useCase = GetTeamDetailUseCase(teamRepository)
    }

    @Test
    fun `invoke returns team from repository`() = runTest {
        val team = Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")
        coEvery { teamRepository.getTeamById(1) } returns team

        val result = useCase(1)

        assertEquals(team, result)
    }
}
