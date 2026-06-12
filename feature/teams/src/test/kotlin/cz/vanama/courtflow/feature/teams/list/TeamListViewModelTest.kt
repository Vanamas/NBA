package cz.vanama.courtflow.feature.teams.list

import app.cash.turbine.test
import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.error.DataException
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetTeamsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
class TeamListViewModelTest {
    private lateinit var getTeamsUseCase: GetTeamsUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val teams = listOf(Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers"))

    private val celtics = Team(1, "BOS", "Boston", "East", "Atlantic", "Boston Celtics", "Celtics")
    private val nets = Team(2, "BKN", "Brooklyn", "East", "Atlantic", "Brooklyn Nets", "Nets")
    private val bucks = Team(3, "MIL", "Milwaukee", "East", "Central", "Milwaukee Bucks", "Bucks")
    private val lakers = Team(4, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")
    private val bullets = Team(5, "BAL", "Baltimore", "", "", "Baltimore Bullets", "Bullets")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamsUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `teams are loaded in init on success`() =
        runTest {
            every { getTeamsUseCase() } returns flowOf(teams)

            val viewModel = TeamListViewModel(getTeamsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(teams, viewModel.uiState.value.teams)
            assertEquals(false, viewModel.uiState.value.isLoading)
            assertEquals(null, viewModel.uiState.value.error)
        }

    @Test
    fun `init updates state with error on failure`() =
        runTest {
            every { getTeamsUseCase() } returns flow { throw DataException(DataErrorKind.SERVER) }

            val viewModel = TeamListViewModel(getTeamsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(DataErrorKind.SERVER, viewModel.uiState.value.error)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }

    @Test
    fun `Retry intent reloads the teams after a failure`() =
        runTest {
            every { getTeamsUseCase() } returns flow { throw DataException(DataErrorKind.SERVER) } andThen flowOf(teams)

            val viewModel = TeamListViewModel(getTeamsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(DataErrorKind.SERVER, viewModel.uiState.value.error)

            viewModel.onIntent(TeamListIntent.Retry)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(teams, viewModel.uiState.value.teams)
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
            every { getTeamsUseCase() } returns flowOf(teams)

            val viewModel = TeamListViewModel(getTeamsUseCase)

            viewModel.uiEffect.test {
                viewModel.onIntent(TeamListIntent.OnTeamClicked(7))
                assertEquals(TeamListEffect.NavigateToTeamDetail(7), awaitItem())
            }
        }
}
