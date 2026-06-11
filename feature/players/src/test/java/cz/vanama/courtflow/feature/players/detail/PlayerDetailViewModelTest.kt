package cz.vanama.courtflow.feature.players.detail

import app.cash.turbine.test
import cz.vanama.courtflow.domain.error.DataException
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetPlayerDetailUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerDetailViewModelTest {
    private lateinit var getPlayerDetailUseCase: GetPlayerDetailUseCase
    private lateinit var viewModel: PlayerDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayerDetailUseCase = mockk()
        viewModel = PlayerDetailViewModel(getPlayerDetailUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LoadPlayer intent updates state with player on success`() =
        runTest {
            val team = Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")
            val player = Player(id = 1, firstName = "LeBron", lastName = "James", position = "F", team = team)
            coEvery { getPlayerDetailUseCase(1) } returns player

            viewModel.onIntent(PlayerDetailIntent.LoadPlayer(1))
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(player, viewModel.uiState.value.player)
            assertEquals(false, viewModel.uiState.value.isLoading)
            assertEquals(null, viewModel.uiState.value.error)
        }

    @Test
    fun `LoadPlayer intent updates state with error on failure`() =
        runTest {
            coEvery { getPlayerDetailUseCase(1) } throws DataException("Error")

            viewModel.onIntent(PlayerDetailIntent.LoadPlayer(1))
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals("Error", viewModel.uiState.value.error)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }

    @Test
    fun `OnTeamClicked intent emits NavigateToTeamDetail effect`() =
        runTest {
            viewModel.uiEffect.test {
                viewModel.onIntent(PlayerDetailIntent.OnTeamClicked(14))
                assertEquals(PlayerDetailEffect.NavigateToTeamDetail(14), awaitItem())
            }
        }
}
