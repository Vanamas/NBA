package cz.vanama.courtflow.feature.teams.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.domain.usecase.GetTeamDetailUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamGamesUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamPlayersUseCase
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
    private val getTeamDetailUseCase: GetTeamDetailUseCase,
    private val getTeamGamesUseCase: GetTeamGamesUseCase,
    getTeamPlayersUseCase: GetTeamPlayersUseCase,
) : ViewModel() {
    val uiState: StateFlow<TeamDetailState>
        field = MutableStateFlow(TeamDetailState(players = getTeamPlayersUseCase(teamId).cachedIn(viewModelScope)))

    val uiEffect: SharedFlow<TeamDetailEffect>
        field = MutableSharedFlow<TeamDetailEffect>()

    init {
        loadTeam()
        loadRecentGames()
    }

    fun onIntent(intent: TeamDetailIntent) {
        when (intent) {
            is TeamDetailIntent.Retry -> {
                loadTeam()
                loadRecentGames()
            }
            is TeamDetailIntent.OnPlayerClicked -> onPlayerClicked(intent.playerId)
            is TeamDetailIntent.OnShareClicked -> shareTeam()
        }
    }

    private fun loadTeam() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val team = getTeamDetailUseCase(teamId)
                uiState.update { it.copy(isLoading = false, team = team) }
            } catch (e: DataException) {
                uiState.update { it.copy(isLoading = false, error = e.kind) }
            }
        }
    }

    private fun loadRecentGames() {
        viewModelScope.launch {
            val games =
                try {
                    getTeamGamesUseCase(teamId)
                } catch (_: DataException) {
                    // Bonus section: on failure keep it hidden and leave the
                    // team info and roster untouched.
                    emptyList()
                }
            uiState.update { it.copy(recentGames = games) }
        }
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
