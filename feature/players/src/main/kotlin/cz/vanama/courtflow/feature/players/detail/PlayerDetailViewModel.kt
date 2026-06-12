package cz.vanama.courtflow.feature.players.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.domain.error.DataException
import cz.vanama.courtflow.domain.usecase.GetPlayerDetailUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _uiState = MutableStateFlow(PlayerDetailState())
    val uiState: StateFlow<PlayerDetailState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<PlayerDetailEffect>()
    val uiEffect: SharedFlow<PlayerDetailEffect> = _uiEffect.asSharedFlow()

    fun onIntent(intent: PlayerDetailIntent) {
        when (intent) {
            is PlayerDetailIntent.LoadPlayer -> loadPlayer(intent.playerId)
            is PlayerDetailIntent.OnTeamClicked -> onTeamClicked(intent.teamId)
        }
    }

    private fun loadPlayer(playerId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val player = getPlayerDetailUseCase(playerId)
                _uiState.update { it.copy(isLoading = false, player = player) }
            } catch (e: DataException) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    private fun onTeamClicked(teamId: Int) {
        viewModelScope.launch {
            _uiEffect.emit(PlayerDetailEffect.NavigateToTeamDetail(teamId))
        }
    }
}
