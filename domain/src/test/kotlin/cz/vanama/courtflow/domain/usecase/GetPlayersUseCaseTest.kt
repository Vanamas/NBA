package cz.vanama.courtflow.domain.usecase

import androidx.paging.PagingData
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.PlayerFilter
import cz.vanama.courtflow.domain.repository.PlayerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class GetPlayersUseCaseTest {
    private val repository: PlayerRepository = mockk()
    private val useCase = GetPlayersUseCase(repository)

    @Test
    fun `invoke delegates the filter to the repository`() {
        val filter = PlayerFilter(query = "curry", teamId = 10, position = "G")
        every { repository.getPlayers(filter) } returns flowOf(PagingData.empty<Player>())

        useCase(filter)

        verify { repository.getPlayers(filter) }
    }

    @Test
    fun `invoke without arguments delegates an empty filter`() {
        every { repository.getPlayers(PlayerFilter()) } returns flowOf(PagingData.empty<Player>())

        useCase()

        verify { repository.getPlayers(PlayerFilter()) }
    }
}
