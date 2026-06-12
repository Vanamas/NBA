package cz.vanama.courtflow.feature.teams.list

import cz.vanama.courtflow.domain.model.Team

/** UI state of the team list screen. */
data class TeamListState(
    val isLoading: Boolean = false,
    val teams: List<Team> = emptyList(),
    val error: String? = null,
)

/** User actions of the team list screen. */
sealed class TeamListIntent {
    data object LoadTeams : TeamListIntent()

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
