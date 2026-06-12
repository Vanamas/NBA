package cz.vanama.courtflow.feature.teams.detail

import cz.vanama.courtflow.domain.model.Team

/** UI state of the team detail screen. */
data class TeamDetailState(
    val isLoading: Boolean = false,
    val team: Team? = null,
    val error: String? = null,
)

/** User actions of the team detail screen; the initial load happens in the ViewModel's `init`. */
sealed class TeamDetailIntent {
    data object Retry : TeamDetailIntent()
}

/** One-shot events emitted by [TeamDetailViewModel]; currently none. */
sealed class TeamDetailEffect
