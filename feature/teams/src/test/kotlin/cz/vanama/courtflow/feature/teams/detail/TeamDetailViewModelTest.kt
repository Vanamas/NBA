package cz.vanama.courtflow.feature.teams.detail

import androidx.paging.PagingData
import app.cash.turbine.test
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.model.Game
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Standing
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetTeamDetailUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamGamesUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamPlayersUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamStandingUseCase
import cz.vanama.courtflow.domain.usecase.IsFavoriteUseCase
import cz.vanama.courtflow.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class TeamDetailViewModelTest {
    private lateinit var getTeamDetailUseCase: GetTeamDetailUseCase
    private lateinit var getTeamGamesUseCase: GetTeamGamesUseCase
    private lateinit var getTeamPlayersUseCase: GetTeamPlayersUseCase
    private lateinit var getTeamStandingUseCase: GetTeamStandingUseCase
    private lateinit var isFavoriteUseCase: IsFavoriteUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private val favoriteFlow = MutableStateFlow(false)
    private val testDispatcher = StandardTestDispatcher()

    private val team =
        Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")
    private val player =
        Player(id = 19, firstName = "Stephen", lastName = "Curry", position = "G", team = team)

    private val visitorTeam =
        Team(10, "GSW", "Golden State", "West", "Pacific", "Golden State Warriors", "Warriors")
    private val game =
        Game(
            id = 1,
            date = "2026-06-10",
            homeTeam = team,
            homeTeamScore = 112,
            visitorTeam = visitorTeam,
            visitorTeamScore = 99,
        )

    private val standing =
        Standing(teamId = 1, wins = 12, losses = 4, conferenceRank = 3, conference = "West")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamDetailUseCase = mockk()
        getTeamGamesUseCase = mockk { coEvery { this@mockk.invoke(any()) } returns emptyList() }
        getTeamPlayersUseCase =
            mockk {
                every { this@mockk.invoke(any()) } returns flowOf(PagingData.from(listOf(player)))
            }
        getTeamStandingUseCase = mockk { coEvery { this@mockk.invoke(any()) } returns null }
        isFavoriteUseCase = mockk()
        toggleFavoriteUseCase = mockk(relaxed = true)
        every { isFavoriteUseCase(any(), FavoriteType.TEAM) } returns favoriteFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() =
        TeamDetailViewModel(
            1,
            getTeamDetailUseCase,
            getTeamGamesUseCase,
            getTeamStandingUseCase,
            getTeamPlayersUseCase,
            isFavoriteUseCase,
            toggleFavoriteUseCase,
        )

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
    fun `recent games populate state on success`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } returns team
            coEvery { getTeamGamesUseCase(1) } returns listOf(game)

            val viewModel = viewModel()

            viewModel.uiState.test {
                testDispatcher.scheduler.advanceUntilIdle()
                val state = expectMostRecentItem()
                assertEquals(listOf(game), state.recentGames)
                assertEquals(team, state.team)
            }
        }

    @Test
    fun `games failure hides the section while the team loads fine`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } returns team
            coEvery { getTeamGamesUseCase(1) } throws DataException(DataErrorKind.SERVER)

            val viewModel = viewModel()

            viewModel.uiState.test {
                testDispatcher.scheduler.advanceUntilIdle()
                val state = expectMostRecentItem()
                assertEquals(emptyList<Game>(), state.recentGames)
                assertEquals(team, state.team)
                assertEquals(null, state.error)
            }
        }

    @Test
    fun `rate limited load counts down and retries automatically`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } throws
                DataException(DataErrorKind.RATE_LIMITED) andThen team
            val viewModel = viewModel()
            runCurrent()

            assertEquals(DataErrorKind.RATE_LIMITED, viewModel.uiState.value.error)
            assertEquals(15, viewModel.uiState.value.retryInSeconds)

            advanceTimeBy(15_000)
            runCurrent()

            coVerify(exactly = 2) { getTeamDetailUseCase(1) }
            assertEquals(team, viewModel.uiState.value.team)
            assertEquals(null, viewModel.uiState.value.retryInSeconds)
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

    @Test
    fun `isFavorite state reflects the favorites flow`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } returns team
            favoriteFlow.value = true

            val viewModel = viewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(true, viewModel.uiState.value.isFavorite)
        }

    @Test
    fun `OnFavoriteToggled delegates to the toggle use case`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } returns team

            val viewModel = viewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onIntent(TeamDetailIntent.OnFavoriteToggled)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { toggleFavoriteUseCase(1, FavoriteType.TEAM) }
        }

    @Test
    fun `standing populates state on success`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } returns team
            coEvery { getTeamStandingUseCase(1) } returns standing

            val viewModel = viewModel()

            viewModel.uiState.test {
                testDispatcher.scheduler.advanceUntilIdle()
                val state = expectMostRecentItem()
                assertEquals(standing, state.standing)
                assertEquals(team, state.team)
            }
        }

    @Test
    fun `standing failure hides the badge while the team loads fine`() =
        runTest {
            coEvery { getTeamDetailUseCase(1) } returns team
            coEvery { getTeamStandingUseCase(1) } throws DataException(DataErrorKind.SERVER)

            val viewModel = viewModel()

            viewModel.uiState.test {
                testDispatcher.scheduler.advanceUntilIdle()
                val state = expectMostRecentItem()
                assertEquals(null, state.standing)
                assertEquals(team, state.team)
                assertEquals(null, state.error)
            }
        }
}
