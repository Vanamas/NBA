package cz.vanama.courtflow.feature.teams.list

import cz.vanama.courtflow.domain.model.Team

/** UI state of the team list screen. */
data class TeamListState(
    val isLoading: Boolean = false,
    val teams: List<Team> = emptyList(),
    val error: String? = null,
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
