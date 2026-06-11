package cz.vanama.courtflow.feature.players.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel of the player list screen.
 *
 * Exposes [uiState] with the paginated player stream and one-shot
 * navigation events through [uiEffect]; all input goes through [onIntent].
 */
class PlayerListViewModel(
    private val getPlayersUseCase: GetPlayersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerListState())
    val uiState: StateFlow<PlayerListState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<PlayerListEffect>()
    val uiEffect: SharedFlow<PlayerListEffect> = _uiEffect.asSharedFlow()

    fun onIntent(intent: PlayerListIntent) {
        when (intent) {
            is PlayerListIntent.LoadPlayers -> loadPlayers()
            is PlayerListIntent.OnPlayerClicked -> onPlayerClicked(intent.playerId)
        }
    }

    private fun loadPlayers() {
        _uiState.update { it.copy(players = getPlayersUseCase()) }
    }

    private fun onPlayerClicked(playerId: Int) {
        viewModelScope.launch {
            _uiEffect.emit(PlayerListEffect.NavigateToPlayerDetail(playerId))
        }
    }
}
