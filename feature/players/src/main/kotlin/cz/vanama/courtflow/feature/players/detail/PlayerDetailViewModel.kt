package cz.vanama.courtflow.feature.players.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.domain.error.DataException
import cz.vanama.courtflow.domain.usecase.GetPlayerDetailUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel of the player detail screen.
 *
 * Loads the player on [PlayerDetailIntent.LoadPlayer] and emits a navigation
 * effect when the user taps the team button.
 */
class PlayerDetailViewModel(
    private val getPlayerDetailUseCase: GetPlayerDetailUseCase,
) : ViewModel() {
    val uiState: StateFlow<PlayerDetailState>
        field = MutableStateFlow(PlayerDetailState())

    val uiEffect: SharedFlow<PlayerDetailEffect>
        field = MutableSharedFlow<PlayerDetailEffect>()

    fun onIntent(intent: PlayerDetailIntent) {
        when (intent) {
            is PlayerDetailIntent.LoadPlayer -> loadPlayer(intent.playerId)
            is PlayerDetailIntent.OnTeamClicked -> onTeamClicked(intent.teamId)
        }
    }

    private fun loadPlayer(playerId: Int) {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val player = getPlayerDetailUseCase(playerId)
                uiState.update { it.copy(isLoading = false, player = player) }
            } catch (e: DataException) {
                uiState.update { it.copy(isLoading = false, error = e.message.orEmpty()) }
            }
        }
    }

    private fun onTeamClicked(teamId: Int) {
        viewModelScope.launch {
            uiEffect.emit(PlayerDetailEffect.NavigateToTeamDetail(teamId))
        }
    }
}
