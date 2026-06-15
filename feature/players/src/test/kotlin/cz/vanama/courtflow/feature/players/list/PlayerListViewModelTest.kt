package cz.vanama.courtflow.feature.players.list

import androidx.paging.PagingData
import app.cash.turbine.test
import cz.vanama.courtflow.core.common.connectivity.ConnectivityObserver
import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.PlayerFilter
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamsUseCase
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
    private lateinit var getTeamsUseCase: GetTeamsUseCase
    private lateinit var viewModel: PlayerListViewModel
    private lateinit var connectivityObserver: FakeConnectivityObserver
    private val favoriteIds = MutableStateFlow<List<Int>>(emptyList())
    private val testDispatcher = StandardTestDispatcher()

    private val lakers =
        Team(
            id = 14,
            abbreviation = "LAL",
            city = "Los Angeles",
            conference = "West",
            division = "Pacific",
            fullName = "Los Angeles Lakers",
            name = "Lakers",
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayersUseCase = mockk()
        observeFavoritesUseCase = mockk()
        getTeamsUseCase = mockk()
        every { observeFavoritesUseCase(FavoriteType.PLAYER) } returns favoriteIds
        every { getTeamsUseCase() } returns flowOf(emptyList())
        connectivityObserver = FakeConnectivityObserver()
        viewModel =
            PlayerListViewModel(getPlayersUseCase, observeFavoritesUseCase, getTeamsUseCase, connectivityObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `paging stream starts on first collection with an empty filter`() =
        runTest {
            every { getPlayersUseCase(PlayerFilter()) } returns flowOf(PagingData.empty<Player>())

            val job = launch { viewModel.uiState.value.players.collect {} }
            advanceTimeBy(301)
            runCurrent()

            verify { getPlayersUseCase(PlayerFilter()) }
            job.cancel()
        }

    @Test
    fun `search query change reloads players with the query after debounce`() =
        runTest {
            every { getPlayersUseCase(any()) } returns flowOf(PagingData.empty<Player>())
            val job = launch { viewModel.uiState.value.players.collect {} }

            viewModel.onIntent(PlayerListIntent.OnSearchQueryChanged("curry"))
            advanceTimeBy(301)
            runCurrent()

            assertEquals("curry", viewModel.uiState.value.searchQuery)
            verify { getPlayersUseCase(PlayerFilter(query = "curry")) }
            job.cancel()
        }

    @Test
    fun `selecting a team reloads players filtered by team id`() =
        runTest {
            every { getPlayersUseCase(any()) } returns flowOf(PagingData.empty<Player>())
            val job = launch { viewModel.uiState.value.players.collect {} }

            viewModel.onIntent(PlayerListIntent.OnTeamSelected(lakers))
            advanceTimeBy(301)
            runCurrent()

            assertEquals(lakers, viewModel.uiState.value.selectedTeam)
            verify { getPlayersUseCase(PlayerFilter(teamId = 14)) }
            job.cancel()
        }

    @Test
    fun `selecting a position reloads players filtered by position`() =
        runTest {
            every { getPlayersUseCase(any()) } returns flowOf(PagingData.empty<Player>())
            val job = launch { viewModel.uiState.value.players.collect {} }

            viewModel.onIntent(PlayerListIntent.OnPositionSelected("G"))
            advanceTimeBy(301)
            runCurrent()

            assertEquals("G", viewModel.uiState.value.selectedPosition)
            verify { getPlayersUseCase(PlayerFilter(position = "G")) }
            job.cancel()
        }

    @Test
    fun `teams are exposed in state for the picker`() =
        runTest {
            every { getPlayersUseCase(any()) } returns flowOf(PagingData.empty<Player>())
            every { getTeamsUseCase() } returns flowOf(listOf(lakers))
            viewModel =
                PlayerListViewModel(getPlayersUseCase, observeFavoritesUseCase, getTeamsUseCase, connectivityObserver)

            runCurrent()

            assertEquals(listOf(lakers), viewModel.uiState.value.teams)
        }

    @Test
    fun `OnPlayerClicked intent emits NavigateToPlayerDetail effect`() =
        runTest {
            every { getPlayersUseCase(any()) } returns flowOf(PagingData.empty<Player>())
            viewModel.uiEffect.test {
                viewModel.onIntent(PlayerListIntent.OnPlayerClicked(1))
                assertEquals(PlayerListEffect.NavigateToPlayerDetail(1), awaitItem())
            }
        }

    @Test
    fun `favoriteIds reflects the observed favorites flow`() =
        runTest {
            every { getPlayersUseCase(any()) } returns flowOf(PagingData.empty<Player>())
            runCurrent()
            assertEquals(emptySet<Int>(), viewModel.uiState.value.favoriteIds)

            favoriteIds.value = listOf(19, 21)
            runCurrent()

            assertEquals(setOf(19, 21), viewModel.uiState.value.favoriteIds)
        }

    @Test
    fun `losing connectivity sets isOffline and regaining clears it`() =
        runTest {
            every { getPlayersUseCase(any()) } returns flowOf(PagingData.empty<Player>())
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
