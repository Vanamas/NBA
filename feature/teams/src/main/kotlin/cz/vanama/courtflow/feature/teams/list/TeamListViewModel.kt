package cz.vanama.courtflow.feature.teams.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.core.common.connectivity.ConnectivityObserver
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.common.time.RateLimitRetryController
import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetTeamsUseCase
import cz.vanama.courtflow.domain.usecase.ObserveFavoritesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel of the team list screen; loads all teams in `init`
 * (and again on [TeamListIntent.Retry]) and emits navigation effects
 * on row taps.
 */
class TeamListViewModel(
    private val getTeamsUseCase: GetTeamsUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {
    val uiState: StateFlow<TeamListState>
        field = MutableStateFlow(TeamListState())

    val uiEffect: SharedFlow<TeamListEffect>
        field = MutableSharedFlow<TeamListEffect>()

    private val rateLimitRetry = RateLimitRetryController()

    init {
        loadTeams()
        observeConnectivity()
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            observeFavoritesUseCase(FavoriteType.TEAM).collect { ids ->
                uiState.update { it.copy(favoriteIds = ids.toSet()) }
            }
        }
    }

    fun onIntent(intent: TeamListIntent) {
        when (intent) {
            is TeamListIntent.Retry -> loadTeams()
            is TeamListIntent.OnToggleFavoritesFilter ->
                uiState.update { it.copy(showFavoritesOnly = !it.showFavoritesOnly) }
            is TeamListIntent.OnTeamClicked -> onTeamClicked(intent.teamId)
        }
    }

    private fun loadTeams() {
        rateLimitRetry.cancel()
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null, retryInSeconds = null) }
            getTeamsUseCase()
                .catch { e ->
                    if (e !is DataException) throw e
                    uiState.update { it.copy(isLoading = false, error = e.kind) }
                    if (e.kind == DataErrorKind.RATE_LIMITED) scheduleRateLimitRetry()
                }.collect { teams ->
                    uiState.update { it.copy(isLoading = false, sections = teams.groupIntoSections()) }
                }
        }
    }

    private fun scheduleRateLimitRetry() {
        rateLimitRetry.schedule(
            scope = viewModelScope,
            onTick = { remaining -> uiState.update { it.copy(retryInSeconds = remaining) } },
            onElapsed = ::loadTeams,
        )
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.isOnline.collect { online ->
                val wasOffline = uiState.value.isOffline
                uiState.update { it.copy(isOffline = !online) }
                if (online && wasOffline && uiState.value.error != null) {
                    loadTeams()
                }
            }
        }
    }

    private fun onTeamClicked(teamId: Int) {
        viewModelScope.launch {
            uiEffect.emit(TeamListEffect.NavigateToTeamDetail(teamId))
        }
    }
}

/**
 * Groups the teams by conference, then division (sections sorted
 * alphabetically, teams inside each section sorted by full name). Teams with
 * a blank conference land in a single trailing fallback section whose
 * [TeamSection.conference] and [TeamSection.division] are blank.
 */
internal fun List<Team>.groupIntoSections(): List<TeamSection> {
    val (known, unknown) = partition { it.conference.isNotBlank() }
    val sections =
        known
            .groupBy { it.conference.trim() to it.division.trim() }
            .map { (key, group) -> TeamSection(key.first, key.second, group.sortedBy(Team::fullName)) }
            .sortedWith(compareBy(TeamSection::conference, TeamSection::division))
    if (unknown.isEmpty()) return sections
    return sections + TeamSection(conference = "", division = "", teams = unknown.sortedBy(Team::fullName))
}
