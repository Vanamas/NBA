package cz.vanama.courtflow.domain.usecase

import androidx.paging.PagingData
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.repository.PlayerRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetPlayersUseCaseTest {
    private lateinit var playerRepository: PlayerRepository
    private lateinit var useCase: GetPlayersUseCase

    @Before
    fun setup() {
        playerRepository = mockk()
        useCase = GetPlayersUseCase(playerRepository)
    }

    @Test
    fun `invoke returns paging data flow from repository`() =
        runTest {
            val player =
                Player(
                    id = 1,
                    firstName = "First",
                    lastName = "Last",
                    position = "G",
                    team = Team(1, "ABB", "City", "Conf", "Div", "Full", "Name"),
                )
            val pagingData = PagingData.from(listOf(player))
            val flow = flowOf(pagingData)
            every { playerRepository.getPlayers() } returns flow

            val result = useCase()

            assertEquals(flow, result)
        }
}
