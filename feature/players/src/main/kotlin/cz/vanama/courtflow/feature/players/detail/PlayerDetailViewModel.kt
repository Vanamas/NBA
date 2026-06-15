package cz.vanama.courtflow.feature.players.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.common.settings.RecentlyViewedStore
import cz.vanama.courtflow.core.common.time.RateLimitRetryController
import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.usecase.GetPlayerDetailUseCase
import cz.vanama.courtflow.domain.usecase.IsFavoriteUseCase
import cz.vanama.courtflow.domain.usecase.ToggleFavoriteUseCase
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
 * [PlayerDetailIntent.Retry]), mirrors the persisted favorite flag into
 * [uiState], and emits a navigation effect when the user taps the team button.
 */
class PlayerDetailViewModel(
    private val playerId: Int,
    private val getPlayerDetailUseCase: GetPlayerDetailUseCase,
    isFavoriteUseCase: IsFavoriteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val recentlyViewedStore: RecentlyViewedStore,
) : ViewModel() {
    val uiState: StateFlow<PlayerDetailState>
        field = MutableStateFlow(PlayerDetailState())

    val uiEffect: SharedFlow<PlayerDetailEffect>
        field = MutableSharedFlow<PlayerDetailEffect>()

    private val rateLimitRetry = RateLimitRetryController()

    init {
        loadPlayer()
        observeFavorite(isFavoriteUseCase)
    }

    fun onIntent(intent: PlayerDetailIntent) {
        when (intent) {
            is PlayerDetailIntent.Retry -> loadPlayer()
            is PlayerDetailIntent.OnTeamClicked -> onTeamClicked(intent.teamId)
            is PlayerDetailIntent.OnShareClicked -> sharePlayer()
            is PlayerDetailIntent.OnFavoriteToggled -> toggleFavorite()
        }
    }

    private fun observeFavorite(isFavoriteUseCase: IsFavoriteUseCase) {
        viewModelScope.launch {
            isFavoriteUseCase(playerId, FavoriteType.PLAYER).collect { favorite ->
                uiState.update { it.copy(isFavorite = favorite) }
            }
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            toggleFavoriteUseCase(playerId, FavoriteType.PLAYER)
        }
    }

    private fun loadPlayer() {
        rateLimitRetry.cancel()
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null, retryInSeconds = null) }
            try {
                val player = getPlayerDetailUseCase(playerId)
                uiState.update { it.copy(isLoading = false, player = player) }
                recentlyViewedStore.recordView(playerId)
            } catch (e: DataException) {
                uiState.update { it.copy(isLoading = false, error = e.kind) }
                if (e.kind == DataErrorKind.RATE_LIMITED) scheduleRateLimitRetry()
            }
        }
    }

    private fun scheduleRateLimitRetry() {
        rateLimitRetry.schedule(
            scope = viewModelScope,
            onTick = { remaining -> uiState.update { it.copy(retryInSeconds = remaining) } },
            onElapsed = ::loadPlayer,
        )
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
