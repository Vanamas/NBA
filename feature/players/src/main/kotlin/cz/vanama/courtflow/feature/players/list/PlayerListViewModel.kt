package cz.vanama.courtflow.feature.players.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cz.vanama.courtflow.core.common.connectivity.ConnectivityObserver
import cz.vanama.courtflow.core.common.time.RateLimitRetryController
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * MVI ViewModel of the player list screen.
 *
 * The paginated player stream starts when the ViewModel is created and is
 * exposed through [uiState]; one-shot navigation events go through
 * [uiEffect] and all input through [onIntent]. The search query is
 * debounced so the API is queried at most ~3× per second while the user
 * types; a blank query means "all players".
 */
class PlayerListViewModel(
    getPlayersUseCase: GetPlayersUseCase,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {
    private val rateLimitRetry = RateLimitRetryController()
    private val searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val players =
        searchQuery
            .debounce(SEARCH_DEBOUNCE_MS.milliseconds)
            .distinctUntilChanged()
            .flatMapLatest { query -> getPlayersUseCase(query.ifBlank { null }) }
            .cachedIn(viewModelScope)

    val uiState: StateFlow<PlayerListState>
        field = MutableStateFlow(PlayerListState(players = players))

    val uiEffect: SharedFlow<PlayerListEffect>
        field = MutableSharedFlow<PlayerListEffect>()

    init {
        viewModelScope.launch {
            connectivityObserver.isOnline.collect { online ->
                uiState.update { it.copy(isOffline = !online) }
            }
        }
    }

    fun onIntent(intent: PlayerListIntent) {
        when (intent) {
            is PlayerListIntent.OnSearchQueryChanged -> onSearchQueryChanged(intent.query)
            is PlayerListIntent.OnPlayerClicked -> onPlayerClicked(intent.playerId)
            is PlayerListIntent.OnRefreshRateLimited -> onRefreshRateLimited(intent.resetEpochSeconds)
            is PlayerListIntent.OnRefreshResolved -> onRefreshResolved()
        }
    }

    private fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        uiState.update { it.copy(searchQuery = query) }
    }

    private fun onPlayerClicked(playerId: Int) {
        viewModelScope.launch {
            uiEffect.emit(PlayerListEffect.NavigateToPlayerDetail(playerId))
        }
    }

    private fun onRefreshRateLimited(resetEpochSeconds: Long?) {
        rateLimitRetry.schedule(
            resetEpochSeconds = resetEpochSeconds,
            scope = viewModelScope,
            onTick = { remaining -> uiState.update { it.copy(retryInSeconds = remaining) } },
            onElapsed = {
                uiState.update { it.copy(retryInSeconds = null) }
                viewModelScope.launch { uiEffect.emit(PlayerListEffect.RetryPaging) }
            },
        )
    }

    private fun onRefreshResolved() {
        rateLimitRetry.cancel()
        uiState.update { it.copy(retryInSeconds = null) }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
