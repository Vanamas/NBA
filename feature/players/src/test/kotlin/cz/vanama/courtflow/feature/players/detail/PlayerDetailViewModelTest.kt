package cz.vanama.courtflow.feature.players.detail

import app.cash.turbine.test
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetPlayerDetailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerDetailViewModelTest {
    private lateinit var getPlayerDetailUseCase: GetPlayerDetailUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val team = Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")
    private val player = Player(id = 1, firstName = "LeBron", lastName = "James", position = "F", team = team)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayerDetailUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `player is loaded in init on success`() =
        runTest {
            coEvery { getPlayerDetailUseCase(1) } returns player

            val viewModel = PlayerDetailViewModel(1, getPlayerDetailUseCase)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(player, viewModel.uiState.value.player)
            assertEquals(false, viewModel.uiState.value.isLoading)
            assertEquals(null, viewModel.uiState.value.error)
        }

    @Test
    fun `init updates state with error on failure`() =
        runTest {
            coEvery { getPlayerDetailUseCase(1) } throws DataException(DataErrorKind.SERVER)

            val viewModel = PlayerDetailViewModel(1, getPlayerDetailUseCase)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(DataErrorKind.SERVER, viewModel.uiState.value.error)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }

    @Test
    fun `Retry intent reloads the player after a failure`() =
        runTest {
            coEvery { getPlayerDetailUseCase(1) } throws DataException(DataErrorKind.SERVER) andThen player

            val viewModel = PlayerDetailViewModel(1, getPlayerDetailUseCase)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(DataErrorKind.SERVER, viewModel.uiState.value.error)

            viewModel.onIntent(PlayerDetailIntent.Retry)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(player, viewModel.uiState.value.player)
            assertEquals(null, viewModel.uiState.value.error)
        }

    @Test
    fun `OnTeamClicked intent emits NavigateToTeamDetail effect`() =
        runTest {
            coEvery { getPlayerDetailUseCase(1) } returns player

            val viewModel = PlayerDetailViewModel(1, getPlayerDetailUseCase)

            viewModel.uiEffect.test {
                viewModel.onIntent(PlayerDetailIntent.OnTeamClicked(14))
                assertEquals(PlayerDetailEffect.NavigateToTeamDetail(14), awaitItem())
            }
        }

    @Test
    fun `OnShareClicked emits Share with the loaded player`() =
        runTest {
            coEvery { getPlayerDetailUseCase(1) } returns player
            val viewModel = PlayerDetailViewModel(1, getPlayerDetailUseCase)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onIntent(PlayerDetailIntent.OnShareClicked)
                testDispatcher.scheduler.advanceUntilIdle()
                assertEquals(PlayerDetailEffect.Share(player), awaitItem())
            }
        }

    @Test
    fun `rate limited load counts down and retries automatically`() =
        runTest {
            coEvery { getPlayerDetailUseCase(1) } throws
                DataException(DataErrorKind.RATE_LIMITED) andThen player
            val viewModel = PlayerDetailViewModel(1, getPlayerDetailUseCase)
            runCurrent()

            assertEquals(DataErrorKind.RATE_LIMITED, viewModel.uiState.value.error)
            assertEquals(15, viewModel.uiState.value.retryInSeconds)

            advanceTimeBy(15_000)
            runCurrent()

            coVerify(exactly = 2) { getPlayerDetailUseCase(1) }
            assertEquals(player, viewModel.uiState.value.player)
            assertEquals(null, viewModel.uiState.value.retryInSeconds)
        }

    @Test
    fun `OnShareClicked is ignored while no player is loaded`() =
        runTest {
            coEvery { getPlayerDetailUseCase(1) } throws DataException(DataErrorKind.SERVER)
            val viewModel = PlayerDetailViewModel(1, getPlayerDetailUseCase)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onIntent(PlayerDetailIntent.OnShareClicked)
                testDispatcher.scheduler.advanceUntilIdle()
                expectNoEvents()
            }
        }
}
