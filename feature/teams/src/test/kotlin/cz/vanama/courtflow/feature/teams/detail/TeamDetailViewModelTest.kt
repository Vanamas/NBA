package cz.vanama.courtflow.feature.teams.detail

import androidx.paging.PagingData
import app.cash.turbine.test
import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.error.DataException
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetTeamDetailUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamPlayersUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class TeamDetailViewModelTest {
    private lateinit var getTeamDetailUseCase: GetTeamDetailUseCase
    private lateinit var getTeamPlayersUseCase: GetTeamPlayersUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val team = Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")
    private val player = Player(id = 19, firstName = "Stephen", lastName = "Curry", position = "G", team = team)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamDetailUseCase = mockk()
        getTeamPlayersUseCase =
            mockk {
                every { this@mockk.invoke(any()) } returns flowOf(PagingData.from(listOf(player)))
            }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = TeamDetailViewModel(1, getTeamDetailUseCase, getTeamPlayersUseCase)

    @Test
    fun `team is loaded in init on success`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } returns team

            val viewModel = viewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(team, viewModel.uiState.value.team)
            assertEquals(false, viewModel.uiState.value.isLoading)
            assertEquals(null, viewModel.uiState.value.error)
        }

    @Test
    fun `init updates state with error on failure`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } throws DataException(DataErrorKind.SERVER)

            val viewModel = viewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(DataErrorKind.SERVER, viewModel.uiState.value.error)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }

    @Test
    fun `Retry intent reloads the team after a failure`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } throws DataException(DataErrorKind.SERVER) andThen team

            val viewModel = viewModel()
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(DataErrorKind.SERVER, viewModel.uiState.value.error)

            viewModel.onIntent(TeamDetailIntent.Retry)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(team, viewModel.uiState.value.team)
            assertEquals(null, viewModel.uiState.value.error)
        }

    @Test
    fun `roster stream comes from GetTeamPlayersUseCase`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } returns team

            viewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            verify { getTeamPlayersUseCase(1) }
        }

    @Test
    fun `OnPlayerClicked emits NavigateToPlayerDetail`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } returns team
            val viewModel = viewModel()

            viewModel.uiEffect.test {
                viewModel.onIntent(TeamDetailIntent.OnPlayerClicked(19))
                testDispatcher.scheduler.advanceUntilIdle()
                assertEquals(TeamDetailEffect.NavigateToPlayerDetail(19), awaitItem())
            }
        }

    @Test
    fun `OnShareClicked emits Share with the loaded team`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } returns team
            val viewModel = viewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onIntent(TeamDetailIntent.OnShareClicked)
                testDispatcher.scheduler.advanceUntilIdle()
                assertEquals(TeamDetailEffect.Share(team), awaitItem())
            }
        }
}
