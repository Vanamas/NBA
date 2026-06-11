package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.repository.PlayerRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetPlayerDetailUseCaseTest {

    private lateinit var playerRepository: PlayerRepository
    private lateinit var useCase: GetPlayerDetailUseCase

    @Before
    fun setup() {
        playerRepository = mockk()
        useCase = GetPlayerDetailUseCase(playerRepository)
    }

    @Test
    fun `invoke returns player from repository`() = runTest {
        val team = Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")
        val player = Player(id = 1, firstName = "LeBron", lastName = "James", position = "F", team = team)
        coEvery { playerRepository.getPlayerById(1) } returns player

        val result = useCase(1)

        assertEquals(player, result)
    }
}
