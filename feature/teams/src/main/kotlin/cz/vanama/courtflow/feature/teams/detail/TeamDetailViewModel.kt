package cz.vanama.courtflow.feature.teams.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.common.time.RateLimitRetryController
import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.usecase.GetTeamPlayersUseCase
import cz.vanama.courtflow.domain.usecase.IsFavoriteUseCase
import cz.vanama.courtflow.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel of the team detail screen; loads the team with [teamId]
 * in `init` and again on [TeamDetailIntent.Retry]. The paginated roster
 * stream starts when the ViewModel is created and is exposed through
 * [uiState]; tapping a roster row emits a navigation effect.
 */
class TeamDetailViewModel(
    private val teamId: Int,
    private val useCases: TeamDetailUseCases,
    getTeamPlayersUseCase: GetTeamPlayersUseCase,
    isFavoriteUseCase: IsFavoriteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {
    val uiState: StateFlow<TeamDetailState>
        field = MutableStateFlow(TeamDetailState(players = getTeamPlayersUseCase(teamId).cachedIn(viewModelScope)))

    val uiEffect: SharedFlow<TeamDetailEffect>
        field = MutableSharedFlow<TeamDetailEffect>()

    private val rateLimitRetry = RateLimitRetryController()

    init {
        loadTeam()
        loadRecentGames()
        loadStanding()
        observeFavorite(isFavoriteUseCase)
    }

    fun onIntent(intent: TeamDetailIntent) {
        when (intent) {
            is TeamDetailIntent.Retry -> {
                loadTeam()
                loadRecentGames()
                loadStanding()
            }
            is TeamDetailIntent.OnPlayerClicked -> onPlayerClicked(intent.playerId)
            is TeamDetailIntent.OnShareClicked -> shareTeam()
            is TeamDetailIntent.OnFavoriteToggled -> toggleFavorite()
        }
    }

    private fun observeFavorite(isFavoriteUseCase: IsFavoriteUseCase) {
        viewModelScope.launch {
            isFavoriteUseCase(teamId, FavoriteType.TEAM).collect { favorite ->
                uiState.update { it.copy(isFavorite = favorite) }
            }
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            toggleFavoriteUseCase(teamId, FavoriteType.TEAM)
        }
    }

    private fun loadTeam() {
        rateLimitRetry.cancel()
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null, retryInSeconds = null) }
            try {
                val team = useCases.getTeamDetail(teamId)
                uiState.update { it.copy(isLoading = false, team = team) }
            } catch (e: DataException) {
                uiState.update { it.copy(isLoading = false, error = e.kind) }
                if (e.kind == DataErrorKind.RATE_LIMITED) scheduleRateLimitRetry()
            }
        }
    }

    private fun loadRecentGames() {
        viewModelScope.launch {
            val games =
                try {
                    useCases.getTeamGames(teamId)
                } catch (_: DataException) {
                    // Bonus section: on failure keep it hidden and leave the
                    // team info and roster untouched.
                    emptyList()
                }
            uiState.update { it.copy(recentGames = games) }
        }
    }

    private fun loadStanding() {
        viewModelScope.launch {
            val standing =
                try {
                    useCases.getTeamStanding(teamId)
                } catch (_: DataException) {
                    // Bonus section: on failure keep the badge hidden and leave
                    // the team info and roster untouched.
                    null
                }
            uiState.update { it.copy(standing = standing) }
        }
    }

    private fun scheduleRateLimitRetry() {
        rateLimitRetry.schedule(
            scope = viewModelScope,
            onTick = { remaining -> uiState.update { it.copy(retryInSeconds = remaining) } },
            onElapsed = ::loadTeam,
        )
    }

    private fun onPlayerClicked(playerId: Int) {
        viewModelScope.launch {
            uiEffect.emit(TeamDetailEffect.NavigateToPlayerDetail(playerId))
        }
    }

    private fun shareTeam() {
        val team = uiState.value.team ?: return
        viewModelScope.launch {
            uiEffect.emit(TeamDetailEffect.Share(team))
        }
    }
}
