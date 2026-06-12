package cz.vanama.courtflow.feature.teams.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.domain.error.DataException
import cz.vanama.courtflow.domain.usecase.GetTeamsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel of the team list screen; loads all teams in `init`
 * (and again on [TeamListIntent.Retry]) and emits navigation effects
 * on row taps.
 */
class TeamListViewModel(
    private val getTeamsUseCase: GetTeamsUseCase,
) : ViewModel() {
    val uiState: StateFlow<TeamListState>
        field = MutableStateFlow(TeamListState())

    val uiEffect: SharedFlow<TeamListEffect>
        field = MutableSharedFlow<TeamListEffect>()

    init {
        loadTeams()
    }

    fun onIntent(intent: TeamListIntent) {
        when (intent) {
            is TeamListIntent.Retry -> loadTeams()
            is TeamListIntent.OnTeamClicked -> onTeamClicked(intent.teamId)
        }
    }

    private fun loadTeams() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            getTeamsUseCase()
                .catch { e ->
                    if (e !is DataException) throw e
                    uiState.update { it.copy(isLoading = false, error = e.kind) }
                }.collect { teams ->
                    uiState.update { it.copy(isLoading = false, teams = teams) }
                }
        }
    }

    private fun onTeamClicked(teamId: Int) {
        viewModelScope.launch {
            uiEffect.emit(TeamListEffect.NavigateToTeamDetail(teamId))
        }
    }
}
