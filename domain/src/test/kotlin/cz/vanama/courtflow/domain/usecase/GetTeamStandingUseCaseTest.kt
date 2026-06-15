package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.Standing
import cz.vanama.courtflow.domain.repository.StandingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetTeamStandingUseCaseTest {
    private lateinit var standingsRepository: StandingsRepository
    private lateinit var useCase: GetTeamStandingUseCase

    @Before
    fun setup() {
        standingsRepository = mockk()
        useCase = GetTeamStandingUseCase(standingsRepository)
    }

    @Test
    fun `invoke returns the team standing from the repository`() =
        runTest {
            val standing = Standing(teamId = 10, wins = 12, losses = 4, conferenceRank = 3, conference = "West")
            coEvery { standingsRepository.getTeamStanding(10) } returns standing

            val result = useCase(10)

            assertEquals(standing, result)
        }

    @Test
    fun `invoke returns null when the repository has no standing`() =
        runTest {
            coEvery { standingsRepository.getTeamStanding(10) } returns null

            assertNull(useCase(10))
        }
}
