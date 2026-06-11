package cz.vanama.courtflow.feature.teams.detail

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
    private lateinit var viewModel: TeamDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamDetailUseCase = mockk()
        viewModel = TeamDetailViewModel(getTeamDetailUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LoadTeam intent updates state with team on success`() = runTest {
        val team = Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")
        coEvery { getTeamDetailUseCase(1) } returns team

        viewModel.onIntent(TeamDetailIntent.LoadTeam(1))
        
        // Initial state is loading: false, but after intent it should be loading: true then false.
        // viewModelScope.launch runs immediately with StandardTestDispatcher if we advance
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(team, viewModel.uiState.value.team)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `LoadTeam intent updates state with error on failure`() = runTest {
        coEvery { getTeamDetailUseCase(1) } throws Exception("Error")

        viewModel.onIntent(TeamDetailIntent.LoadTeam(1))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Error", viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }
}
