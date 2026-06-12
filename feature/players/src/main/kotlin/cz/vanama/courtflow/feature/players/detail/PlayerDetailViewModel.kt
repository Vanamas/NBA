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
 * Loads the player with [playerId] in `init` (and again on
 * [PlayerDetailIntent.Retry]) and emits a navigation effect when the
 * user taps the team button.
 */
class PlayerDetailViewModel(
    private val playerId: Int,
    private val getPlayerDetailUseCase: GetPlayerDetailUseCase,
) : ViewModel() {
    val uiState: StateFlow<PlayerDetailState>
        field = MutableStateFlow(PlayerDetailState())

    val uiEffect: SharedFlow<PlayerDetailEffect>
        field = MutableSharedFlow<PlayerDetailEffect>()

    init {
        loadPlayer()
    }

    fun onIntent(intent: PlayerDetailIntent) {
        when (intent) {
            is PlayerDetailIntent.Retry -> loadPlayer()
            is PlayerDetailIntent.OnTeamClicked -> onTeamClicked(intent.teamId)
            is PlayerDetailIntent.OnShareClicked -> sharePlayer()
        }
    }

    private fun loadPlayer() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val player = getPlayerDetailUseCase(playerId)
                uiState.update { it.copy(isLoading = false, player = player) }
            } catch (e: DataException) {
                uiState.update { it.copy(isLoading = false, error = e.kind) }
            }
        }
    }

    private fun onTeamClicked(teamId: Int) {
        viewModelScope.launch {
            uiEffect.emit(PlayerDetailEffect.NavigateToTeamDetail(teamId))
        }
    }

    private fun sharePlayer() {
        val player = uiState.value.player ?: return
        viewModelScope.launch {
            uiEffect.emit(PlayerDetailEffect.Share(player))
        }
    }
}
