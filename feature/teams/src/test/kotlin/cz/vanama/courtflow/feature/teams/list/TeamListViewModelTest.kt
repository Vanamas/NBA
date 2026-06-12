package cz.vanama.courtflow.feature.teams.list

import app.cash.turbine.test
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
    private lateinit var viewModel: TeamListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamsUseCase = mockk()
        viewModel = TeamListViewModel(getTeamsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LoadTeams intent updates state with teams on success`() =
        runTest {
            val teams = listOf(Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers"))
            every { getTeamsUseCase() } returns flowOf(teams)

            viewModel.onIntent(TeamListIntent.LoadTeams)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(teams, viewModel.uiState.value.teams)
            assertEquals(false, viewModel.uiState.value.isLoading)
            assertEquals(null, viewModel.uiState.value.error)
        }

    @Test
    fun `LoadTeams intent updates state with error on failure`() =
        runTest {
            every { getTeamsUseCase() } returns flow { throw DataException("Error") }

            viewModel.onIntent(TeamListIntent.LoadTeams)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals("Error", viewModel.uiState.value.error)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }

    @Test
    fun `OnTeamClicked intent emits NavigateToTeamDetail effect`() =
        runTest {
            viewModel.uiEffect.test {
                viewModel.onIntent(TeamListIntent.OnTeamClicked(7))
                assertEquals(TeamListEffect.NavigateToTeamDetail(7), awaitItem())
            }
        }
}
