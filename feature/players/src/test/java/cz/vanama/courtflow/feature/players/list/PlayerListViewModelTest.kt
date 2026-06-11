package cz.vanama.courtflow.feature.players.list

import androidx.paging.PagingData
import app.cash.turbine.test
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerListViewModelTest {
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var viewModel: PlayerListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayersUseCase = mockk()
        viewModel = PlayerListViewModel(getPlayersUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LoadPlayers intent updates state with players flow`() =
        runTest {
            val playersFlow = flowOf(PagingData.empty<Player>())
            every { getPlayersUseCase() } returns playersFlow

            viewModel.onIntent(PlayerListIntent.LoadPlayers)

            assertEquals(playersFlow, viewModel.uiState.value.players)
        }

    @Test
    fun `OnPlayerClicked intent emits NavigateToPlayerDetail effect`() =
        runTest {
            viewModel.uiEffect.test {
                viewModel.onIntent(PlayerListIntent.OnPlayerClicked(1))
                assertEquals(PlayerListEffect.NavigateToPlayerDetail(1), awaitItem())
            }
        }
}
