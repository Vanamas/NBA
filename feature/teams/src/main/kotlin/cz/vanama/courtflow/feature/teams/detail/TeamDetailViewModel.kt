package cz.vanama.courtflow.feature.teams.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.domain.error.DataException
import cz.vanama.courtflow.domain.usecase.GetTeamDetailUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel of the team detail screen; loads the team on
 * [TeamDetailIntent.LoadTeam].
 */
class TeamDetailViewModel(
    private val getTeamDetailUseCase: GetTeamDetailUseCase,
) : ViewModel() {
    val uiState: StateFlow<TeamDetailState>
        field = MutableStateFlow(TeamDetailState())

    val uiEffect: SharedFlow<TeamDetailEffect>
        field = MutableSharedFlow<TeamDetailEffect>()

    fun onIntent(intent: TeamDetailIntent) {
        when (intent) {
            is TeamDetailIntent.LoadTeam -> loadTeam(intent.teamId)
        }
    }

    private fun loadTeam(teamId: Int) {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val team = getTeamDetailUseCase(teamId)
                uiState.update { it.copy(isLoading = false, team = team) }
            } catch (e: DataException) {
                uiState.update { it.copy(isLoading = false, error = e.message.orEmpty()) }
            }
        }
    }
}
