package cz.vanama.courtflow.feature.players.list

import androidx.paging.PagingData
import app.cash.turbine.test
import cz.vanama.courtflow.core.common.connectivity.ConnectivityObserver
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
class PlayerListViewModelTest {
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var viewModel: PlayerListViewModel
    private lateinit var connectivityObserver: FakeConnectivityObserver
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayersUseCase = mockk()
        connectivityObserver = FakeConnectivityObserver()
        viewModel = PlayerListViewModel(getPlayersUseCase, connectivityObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `paging stream starts on first collection`() =
        runTest {
            every { getPlayersUseCase(null) } returns flowOf(PagingData.empty<Player>())

            val job =
                launch {
                    viewModel.uiState.value.players
                        .collect {}
                }
            advanceTimeBy(301)
            runCurrent()

            verify { getPlayersUseCase(null) }
            job.cancel()
        }

    @Test
    fun `search query change reloads players with the query after debounce`() =
        runTest {
            every { getPlayersUseCase(any()) } returns flowOf(PagingData.empty<Player>())
            val job =
                launch {
                    viewModel.uiState.value.players
                        .collect {}
                }

            viewModel.onIntent(PlayerListIntent.OnSearchQueryChanged("curry"))
            advanceTimeBy(301)
            runCurrent()

            assertEquals("curry", viewModel.uiState.value.searchQuery)
            verify { getPlayersUseCase("curry") }
            job.cancel()
        }

    @Test
    fun `OnPlayerClicked intent emits NavigateToPlayerDetail effect`() =
        runTest {
            viewModel.uiEffect.test {
                viewModel.onIntent(PlayerListIntent.OnPlayerClicked(1))
                assertEquals(PlayerListEffect.NavigateToPlayerDetail(1), awaitItem())
            }
        }

    @Test
    fun `losing connectivity sets isOffline and regaining clears it`() =
        runTest {
            runCurrent()

            connectivityObserver.online.value = false
            runCurrent()
            assertEquals(true, viewModel.uiState.value.isOffline)

            connectivityObserver.online.value = true
            runCurrent()
            assertEquals(false, viewModel.uiState.value.isOffline)
        }

    @Test
    fun `OnRefreshRateLimited with null epoch starts the fallback countdown`() =
        runTest {
            viewModel.onIntent(PlayerListIntent.OnRefreshRateLimited(null))
            runCurrent()

            assertEquals(15, viewModel.uiState.value.retryInSeconds)
        }

    @Test
    fun `OnRefreshRateLimited with a future reset caps the countdown at 60s`() =
        runTest {
            viewModel.onIntent(PlayerListIntent.OnRefreshRateLimited(4_000_000_000L))
            runCurrent()

            assertEquals(60, viewModel.uiState.value.retryInSeconds)
        }

    @Test
    fun `countdown elapsing emits RetryPaging and clears retryInSeconds`() =
        runTest {
            viewModel.uiEffect.test {
                viewModel.onIntent(PlayerListIntent.OnRefreshRateLimited(null))
                advanceTimeBy(15_000)
                runCurrent()
                assertEquals(PlayerListEffect.RetryPaging, awaitItem())
            }
            assertEquals(null, viewModel.uiState.value.retryInSeconds)
        }

    @Test
    fun `OnRefreshResolved cancels the countdown and clears retryInSeconds`() =
        runTest {
            viewModel.onIntent(PlayerListIntent.OnRefreshRateLimited(null))
            runCurrent()
            assertEquals(15, viewModel.uiState.value.retryInSeconds)

            viewModel.onIntent(PlayerListIntent.OnRefreshResolved)
            runCurrent()
            assertEquals(null, viewModel.uiState.value.retryInSeconds)

            viewModel.uiEffect.test {
                advanceTimeBy(15_000)
                runCurrent()
                expectNoEvents()
            }
        }
}

private class FakeConnectivityObserver(
    initiallyOnline: Boolean = true,
) : ConnectivityObserver {
    val online = MutableStateFlow(initiallyOnline)
    override val isOnline: Flow<Boolean> = online
}
