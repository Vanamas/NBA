package cz.vanama.courtflow.feature.players.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cz.vanama.courtflow.core.common.connectivity.ConnectivityObserver
import cz.vanama.courtflow.core.common.time.RateLimitRetryController
import cz.vanama.courtflow.domain.model.PlayerFilter
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamsUseCase
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
 * exposed through [uiState]; one-shot navigation events go through [uiEffect]
 * and all input through [onIntent]. The [PlayerFilter] (search query, team and
 * position) is debounced so the API is queried at most ~3x per second while
 * the user types or taps chips; an empty filter means "all players" and is
 * served offline-first.
 */
class PlayerListViewModel(
    getPlayersUseCase: GetPlayersUseCase,
    getTeamsUseCase: GetTeamsUseCase,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {
    private val rateLimitRetry = RateLimitRetryController()
    private val filter = MutableStateFlow(PlayerFilter())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val players =
        filter
            .debounce(FILTER_DEBOUNCE_MS.milliseconds)
            .distinctUntilChanged()
            .flatMapLatest { current -> getPlayersUseCase(current) }
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
        viewModelScope.launch {
            getTeamsUseCase().collect { teams ->
                uiState.update { it.copy(teams = teams) }
            }
        }
    }

    fun onIntent(intent: PlayerListIntent) {
        when (intent) {
            is PlayerListIntent.OnSearchQueryChanged -> onSearchQueryChanged(intent.query)
            is PlayerListIntent.OnTeamSelected -> onTeamSelected(intent.team)
            is PlayerListIntent.OnPositionSelected -> onPositionSelected(intent.position)
            is PlayerListIntent.OnPlayerClicked -> onPlayerClicked(intent.playerId)
            is PlayerListIntent.OnRateLimited -> onRateLimited(intent.resetEpochSeconds)
            is PlayerListIntent.OnRateLimitResolved -> onRateLimitResolved()
        }
    }

    private fun onSearchQueryChanged(query: String) {
        filter.update { it.copy(query = query) }
        uiState.update { it.copy(searchQuery = query) }
    }

    private fun onTeamSelected(team: Team?) {
        filter.update { it.copy(teamId = team?.id) }
        uiState.update { it.copy(selectedTeam = team) }
    }

    private fun onPositionSelected(position: String?) {
        filter.update { it.copy(position = position) }
        uiState.update { it.copy(selectedPosition = position) }
    }

    private fun onPlayerClicked(playerId: Int) {
        viewModelScope.launch {
            uiEffect.emit(PlayerListEffect.NavigateToPlayerDetail(playerId))
        }
    }

    private fun onRateLimited(resetEpochSeconds: Long?) {
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

    private fun onRateLimitResolved() {
        rateLimitRetry.cancel()
        uiState.update { it.copy(retryInSeconds = null) }
    }

    private companion object {
        const val FILTER_DEBOUNCE_MS = 300L
    }
}
