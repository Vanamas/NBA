package cz.vanama.courtflow.feature.teams.list

import app.cash.turbine.test
import cz.vanama.courtflow.core.common.connectivity.ConnectivityObserver
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetTeamsUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
class TeamListViewModelTest {
    private lateinit var getTeamsUseCase: GetTeamsUseCase
    private lateinit var connectivityObserver: FakeConnectivityObserver
    private val testDispatcher = StandardTestDispatcher()

    private val celtics = Team(1, "BOS", "Boston", "East", "Atlantic", "Boston Celtics", "Celtics")
    private val nets = Team(2, "BKN", "Brooklyn", "East", "Atlantic", "Brooklyn Nets", "Nets")
    private val bucks = Team(3, "MIL", "Milwaukee", "East", "Central", "Milwaukee Bucks", "Bucks")
    private val lakers = Team(4, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")
    private val bullets = Team(5, "BAL", "Baltimore", "", "", "Baltimore Bullets", "Bullets")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamsUseCase = mockk()
        connectivityObserver = FakeConnectivityObserver()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `teams are loaded and grouped in init on success`() =
        runTest {
            every { getTeamsUseCase() } returns flowOf(listOf(lakers, celtics))

            val viewModel = TeamListViewModel(getTeamsUseCase, connectivityObserver)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem() // initial state
                testDispatcher.scheduler.advanceUntilIdle()
                val loaded = expectMostRecentItem()
                assertEquals(
                    listOf(
                        TeamSection("East", "Atlantic", listOf(celtics)),
                        TeamSection("West", "Pacific", listOf(lakers)),
                    ),
                    loaded.sections,
                )
                assertEquals(false, loaded.isLoading)
                assertEquals(null, loaded.error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `blank-conference teams land in the fallback section of the state`() =
        runTest {
            every { getTeamsUseCase() } returns flowOf(listOf(bullets, lakers))

            val viewModel = TeamListViewModel(getTeamsUseCase, connectivityObserver)

            viewModel.uiState.test {
                awaitItem() // initial state
                testDispatcher.scheduler.advanceUntilIdle()
                val loaded = expectMostRecentItem()
                assertEquals(
                    listOf(
                        TeamSection("West", "Pacific", listOf(lakers)),
                        TeamSection("", "", listOf(bullets)),
                    ),
                    loaded.sections,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init updates state with error on failure`() =
        runTest {
            every { getTeamsUseCase() } returns flow { throw DataException(DataErrorKind.SERVER) }

            val viewModel = TeamListViewModel(getTeamsUseCase, connectivityObserver)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(DataErrorKind.SERVER, viewModel.uiState.value.error)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }

    @Test
    fun `Retry intent reloads the teams after a failure`() =
        runTest {
            every { getTeamsUseCase() } returns
                flow { throw DataException(DataErrorKind.SERVER) } andThen flowOf(listOf(lakers))

            val viewModel = TeamListViewModel(getTeamsUseCase, connectivityObserver)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(DataErrorKind.SERVER, viewModel.uiState.value.error)

            viewModel.onIntent(TeamListIntent.Retry)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(listOf(TeamSection("West", "Pacific", listOf(lakers))), viewModel.uiState.value.sections)
            assertEquals(null, viewModel.uiState.value.error)
        }

    @Test
    fun `groupIntoSections groups by conference then division and sorts deterministically`() {
        val sections = listOf(lakers, bucks, nets, celtics).groupIntoSections()

        assertEquals(
            listOf(
                TeamSection("East", "Atlantic", listOf(celtics, nets)),
                TeamSection("East", "Central", listOf(bucks)),
                TeamSection("West", "Pacific", listOf(lakers)),
            ),
            sections,
        )
    }

    @Test
    fun `groupIntoSections puts blank-conference teams into a trailing fallback section`() {
        val sections = listOf(bullets, lakers).groupIntoSections()

        assertEquals(
            listOf(
                TeamSection("West", "Pacific", listOf(lakers)),
                TeamSection("", "", listOf(bullets)),
            ),
            sections,
        )
    }

    @Test
    fun `groupIntoSections returns no fallback section when all conferences are present`() {
        val sections = listOf(lakers).groupIntoSections()

        assertEquals(listOf(TeamSection("West", "Pacific", listOf(lakers))), sections)
    }

    @Test
    fun `OnTeamClicked intent emits NavigateToTeamDetail effect`() =
        runTest {
            every { getTeamsUseCase() } returns flowOf(listOf(lakers))

            val viewModel = TeamListViewModel(getTeamsUseCase, connectivityObserver)

            viewModel.uiEffect.test {
                viewModel.onIntent(TeamListIntent.OnTeamClicked(7))
                assertEquals(TeamListEffect.NavigateToTeamDetail(7), awaitItem())
            }
        }

    @Test
    fun `rate limited load counts down and retries automatically`() =
        runTest {
            every { getTeamsUseCase() } returns
                flow { throw DataException(DataErrorKind.RATE_LIMITED) } andThen
                flowOf(teams)
            val viewModel = TeamListViewModel(getTeamsUseCase, FakeConnectivityObserver())
            runCurrent()

            assertEquals(DataErrorKind.RATE_LIMITED, viewModel.uiState.value.error)
            assertEquals(15, viewModel.uiState.value.retryInSeconds)

            advanceTimeBy(15_000)
            runCurrent()

            verify(exactly = 2) { getTeamsUseCase() }
            assertEquals(teams, viewModel.uiState.value.teams)
            assertEquals(null, viewModel.uiState.value.retryInSeconds)
        }

    @Test
    fun `manual retry during countdown cancels the scheduled auto-retry`() =
        runTest {
            every { getTeamsUseCase() } returns
                flow { throw DataException(DataErrorKind.RATE_LIMITED) } andThen
                flowOf(teams)

            val viewModel = TeamListViewModel(getTeamsUseCase, connectivityObserver)
            runCurrent()

            assertEquals(15, viewModel.uiState.value.retryInSeconds)

            advanceTimeBy(5_000)
            runCurrent()
            viewModel.onIntent(TeamListIntent.Retry)
            runCurrent()

            verify(exactly = 2) { getTeamsUseCase() }
            assertEquals(teams, viewModel.uiState.value.teams)

            advanceTimeBy(20_000)
            runCurrent()

            // Cancelled countdown never fired a third load
            verify(exactly = 2) { getTeamsUseCase() }
            assertEquals(null, viewModel.uiState.value.retryInSeconds)
        }

    @Test
    fun `reconnecting after a failed load retries automatically`() =
        runTest {
            every { getTeamsUseCase() } returns
                flow { throw DataException(DataErrorKind.NETWORK) } andThen
                flowOf(teams)
            val connectivity = FakeConnectivityObserver(initiallyOnline = true)
            val viewModel = TeamListViewModel(getTeamsUseCase, connectivity)
            runCurrent()
            assertEquals(DataErrorKind.NETWORK, viewModel.uiState.value.error)

            connectivity.online.value = false
            runCurrent()
            assertEquals(true, viewModel.uiState.value.isOffline)

            connectivity.online.value = true
            runCurrent()

            verify(exactly = 2) { getTeamsUseCase() }
            assertEquals(teams, viewModel.uiState.value.teams)
        }
}

private class FakeConnectivityObserver(
    initiallyOnline: Boolean = true,
) : ConnectivityObserver {
    val online = MutableStateFlow(initiallyOnline)
    override val isOnline: Flow<Boolean> = online
}
