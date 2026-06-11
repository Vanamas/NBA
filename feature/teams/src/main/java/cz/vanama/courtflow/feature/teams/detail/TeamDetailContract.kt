package cz.vanama.courtflow.feature.teams.detail

import cz.vanama.courtflow.domain.model.Team

/** UI state of the team detail screen. */
data class TeamDetailState(
    val isLoading: Boolean = false,
    val team: Team? = null,
    val error: String? = null
)

/** User actions of the team detail screen. */
sealed class TeamDetailIntent {
    data class LoadTeam(val teamId: Int) : TeamDetailIntent()
}

/** One-shot events emitted by [TeamDetailViewModel]; currently none. */
sealed class TeamDetailEffect
