package cz.vanama.courtflow.feature.players.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * MVI ViewModel of the player list screen.
 *
 * Exposes [uiState] with the paginated player stream and one-shot
 * navigation events through [uiEffect]; all input goes through [onIntent].
 * The search query is debounced so the API is queried at most ~3× per
 * second while the user types; a blank query means "all players".
 */
class PlayerListViewModel(
    private val getPlayersUseCase: GetPlayersUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerListState())
    val uiState: StateFlow<PlayerListState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<PlayerListEffect>()
    val uiEffect: SharedFlow<PlayerListEffect> = _uiEffect.asSharedFlow()

    private val searchQuery = MutableStateFlow("")

    fun onIntent(intent: PlayerListIntent) {
        when (intent) {
            is PlayerListIntent.LoadPlayers -> loadPlayers()
            is PlayerListIntent.OnSearchQueryChanged -> onSearchQueryChanged(intent.query)
            is PlayerListIntent.OnPlayerClicked -> onPlayerClicked(intent.playerId)
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun loadPlayers() {
        val players =
            searchQuery
                .debounce(SEARCH_DEBOUNCE_MS.milliseconds)
                .distinctUntilChanged()
                .flatMapLatest { query -> getPlayersUseCase(query.ifBlank { null }) }
                .cachedIn(viewModelScope)
        _uiState.update { it.copy(players = players) }
    }

    private fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun onPlayerClicked(playerId: Int) {
        viewModelScope.launch {
            _uiEffect.emit(PlayerListEffect.NavigateToPlayerDetail(playerId))
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
