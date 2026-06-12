package cz.vanama.courtflow.feature.teams.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.domain.error.DataException
import cz.vanama.courtflow.domain.usecase.GetTeamsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel of the team list screen; loads all teams on
 * [TeamListIntent.LoadTeams] and emits navigation effects on row taps.
 */
class TeamListViewModel(
    private val getTeamsUseCase: GetTeamsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamListState())
    val uiState: StateFlow<TeamListState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<TeamListEffect>()
    val uiEffect: SharedFlow<TeamListEffect> = _uiEffect.asSharedFlow()

    fun onIntent(intent: TeamListIntent) {
        when (intent) {
            is TeamListIntent.LoadTeams -> loadTeams()
            is TeamListIntent.OnTeamClicked -> onTeamClicked(intent.teamId)
        }
    }

    private fun loadTeams() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getTeamsUseCase()
                .catch { e ->
                    if (e !is DataException) throw e
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
                }.collect { teams ->
                    _uiState.update { it.copy(isLoading = false, teams = teams) }
                }
        }
    }

    private fun onTeamClicked(teamId: Int) {
        viewModelScope.launch {
            _uiEffect.emit(TeamListEffect.NavigateToTeamDetail(teamId))
        }
    }
}
