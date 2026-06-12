package cz.vanama.courtflow.feature.players.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.common.time.countdownSeconds
import cz.vanama.courtflow.domain.usecase.GetPlayerDetailUseCase
import kotlinx.coroutines.Job
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

    private var rateLimitRetryJob: Job? = null

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
        rateLimitRetryJob?.cancel()
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null, retryInSeconds = null) }
            try {
                val player = getPlayerDetailUseCase(playerId)
                uiState.update { it.copy(isLoading = false, player = player) }
            } catch (e: DataException) {
                uiState.update { it.copy(isLoading = false, error = e.kind) }
                if (e.kind == DataErrorKind.RATE_LIMITED) scheduleRateLimitRetry()
            }
        }
    }

    private fun scheduleRateLimitRetry() {
        rateLimitRetryJob =
            viewModelScope.launch {
                countdownSeconds(RATE_LIMIT_RETRY_SECONDS).collect { remaining ->
                    uiState.update { it.copy(retryInSeconds = remaining.takeIf { s -> s > 0 }) }
                    if (remaining == 0) loadPlayer()
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

    private companion object {
        /** balldontlie's free tier limits requests per minute; 15 s is a safe wait. */
        const val RATE_LIMIT_RETRY_SECONDS = 15
    }
}
