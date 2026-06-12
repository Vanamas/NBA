package cz.vanama.courtflow.feature.teams.detail

import cz.vanama.courtflow.domain.error.DataException
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetTeamDetailUseCase
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
class TeamDetailViewModelTest {
    private lateinit var getTeamDetailUseCase: GetTeamDetailUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val team = Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamDetailUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `team is loaded in init on success`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } returns team

            val viewModel = TeamDetailViewModel(1, getTeamDetailUseCase)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(team, viewModel.uiState.value.team)
            assertEquals(false, viewModel.uiState.value.isLoading)
            assertEquals(null, viewModel.uiState.value.error)
        }

    @Test
    fun `init updates state with error on failure`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } throws DataException("Error")

            val viewModel = TeamDetailViewModel(1, getTeamDetailUseCase)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals("Error", viewModel.uiState.value.error)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }

    @Test
    fun `Retry intent reloads the team after a failure`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } throws DataException("Error") andThen team

            val viewModel = TeamDetailViewModel(1, getTeamDetailUseCase)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals("Error", viewModel.uiState.value.error)

            viewModel.onIntent(TeamDetailIntent.Retry)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(team, viewModel.uiState.value.team)
            assertEquals(null, viewModel.uiState.value.error)
        }
}
