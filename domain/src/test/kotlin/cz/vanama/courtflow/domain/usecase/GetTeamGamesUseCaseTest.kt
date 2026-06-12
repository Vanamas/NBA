package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.Game
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.repository.GameRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetTeamGamesUseCaseTest {
    private lateinit var gameRepository: GameRepository
    private lateinit var useCase: GetTeamGamesUseCase

    private val warriors =
        Team(10, "GSW", "Golden State", "West", "Pacific", "Golden State Warriors", "Warriors")
    private val lakers =
        Team(14, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")

    @Before
    fun setup() {
        gameRepository = mockk()
        useCase = GetTeamGamesUseCase(gameRepository)
    }

    @Test
    fun `invoke returns recent games from repository`() =
        runTest {
            val games =
                listOf(
                    Game(
                        id = 1,
                        date = "2026-06-10",
                        homeTeam = warriors,
                        homeTeamScore = 112,
                        visitorTeam = lakers,
                        visitorTeamScore = 99,
                    ),
                )
            coEvery { gameRepository.getRecentGames(10) } returns games

            val result = useCase(10)

            assertEquals(games, result)
        }
}
