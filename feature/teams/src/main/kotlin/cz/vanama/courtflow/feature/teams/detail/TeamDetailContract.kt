package cz.vanama.courtflow.feature.teams.detail

import androidx.paging.PagingData
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.domain.model.Game
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * UI state of the team detail screen; the roster stream starts in the ViewModel's
 * `init`. [recentGames] is a bonus section: empty (and hidden) until loaded,
 * and kept empty when its request fails so the rest of the screen is untouched.
 */
data class TeamDetailState(
    val isLoading: Boolean = false,
    val team: Team? = null,
    val players: Flow<PagingData<Player>> = emptyFlow(),
    val recentGames: List<Game> = emptyList(),
    val error: DataErrorKind? = null,
    val retryInSeconds: Int? = null,
)

/** User actions of the team detail screen; the initial load happens in the ViewModel's `init`. */
sealed class TeamDetailIntent {
    data object Retry : TeamDetailIntent()

    data object OnShareClicked : TeamDetailIntent()

    data class OnPlayerClicked(
        val playerId: Int,
    ) : TeamDetailIntent()
}

/** One-shot events emitted by [TeamDetailViewModel]. */
sealed class TeamDetailEffect {
    data class NavigateToPlayerDetail(
        val playerId: Int,
    ) : TeamDetailEffect()

    data class Share(
        val team: Team,
    ) : TeamDetailEffect()
}
