package cz.vanama.courtflow.feature.teams.list

import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.domain.model.Team

/** UI state of the team list screen. */
data class TeamListState(
    val isLoading: Boolean = false,
    val sections: List<TeamSection> = emptyList(),
    val error: DataErrorKind? = null,
    val isOffline: Boolean = false,
    val retryInSeconds: Int? = null,
    val favoriteIds: Set<Int> = emptySet(),
)

/** User actions of the team list screen; the initial load happens in the ViewModel's `init`. */
sealed class TeamListIntent {
    data object Retry : TeamListIntent()

    data class OnTeamClicked(
        val teamId: Int,
    ) : TeamListIntent()
}

/** One-shot events emitted by [TeamListViewModel]. */
sealed class TeamListEffect {
    data class NavigateToTeamDetail(
        val teamId: Int,
    ) : TeamListEffect()
}

/**
 * One group of teams sharing a conference and division. [conference] and
 * [division] hold the raw API strings (both blank for the fallback section
 * of historical teams) — mapping to display text happens in the UI layer.
 */
data class TeamSection(
    val conference: String,
    val division: String,
    val teams: List<Team>,
)
