package cz.vanama.courtflow.feature.teams.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.domain.error.DataException
import cz.vanama.courtflow.domain.usecase.GetTeamDetailUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel of the team detail screen; loads the team on
 * [TeamDetailIntent.LoadTeam].
 */
class TeamDetailViewModel(
    private val getTeamDetailUseCase: GetTeamDetailUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamDetailState())
    val uiState: StateFlow<TeamDetailState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<TeamDetailEffect>()
    val uiEffect: SharedFlow<TeamDetailEffect> = _uiEffect.asSharedFlow()

    fun onIntent(intent: TeamDetailIntent) {
        when (intent) {
            is TeamDetailIntent.LoadTeam -> loadTeam(intent.teamId)
        }
    }

    private fun loadTeam(teamId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val team = getTeamDetailUseCase(teamId)
                _uiState.update { it.copy(isLoading = false, team = team) }
            } catch (e: DataException) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }
}
