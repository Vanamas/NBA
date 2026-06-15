package cz.vanama.courtflow.feature.players.list

import androidx.paging.PagingData
import app.cash.turbine.test
import cz.vanama.courtflow.core.common.connectivity.ConnectivityObserver
import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import cz.vanama.courtflow.domain.usecase.ObserveFavoritesUseCase
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
    private lateinit var observeFavoritesUseCase: ObserveFavoritesUseCase
    private lateinit var viewModel: PlayerListViewModel
    private lateinit var connectivityObserver: FakeConnectivityObserver
    private val favoriteIds = MutableStateFlow<List<Int>>(emptyList())
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayersUseCase = mockk()
        observeFavoritesUseCase = mockk()
        every { observeFavoritesUseCase(FavoriteType.PLAYER) } returns favoriteIds
        connectivityObserver = FakeConnectivityObserver()
        viewModel = PlayerListViewModel(getPlayersUseCase, observeFavoritesUseCase, connectivityObserver)
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
    fun `favoriteIds reflects the observed favorites flow`() =
        runTest {
            runCurrent()
            assertEquals(emptySet<Int>(), viewModel.uiState.value.favoriteIds)

            favoriteIds.value = listOf(19, 21)
            runCurrent()

            assertEquals(setOf(19, 21), viewModel.uiState.value.favoriteIds)
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
}

private class FakeConnectivityObserver(
    initiallyOnline: Boolean = true,
) : ConnectivityObserver {
    val online = MutableStateFlow(initiallyOnline)
    override val isOnline: Flow<Boolean> = online
}
