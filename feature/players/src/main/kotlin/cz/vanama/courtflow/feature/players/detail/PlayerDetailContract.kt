package cz.vanama.courtflow.feature.players.detail

import cz.vanama.courtflow.domain.model.Player

/** UI state of the player detail screen. */
data class PlayerDetailState(
    val isLoading: Boolean = false,
    val player: Player? = null,
    val error: String? = null,
)

/** User actions of the player detail screen; the initial load happens in the ViewModel's `init`. */
sealed class PlayerDetailIntent {
    data object Retry : PlayerDetailIntent()

    data class OnTeamClicked(
        val teamId: Int,
    ) : PlayerDetailIntent()
}

/** One-shot events emitted by [PlayerDetailViewModel]. */
sealed class PlayerDetailEffect {
    data class NavigateToTeamDetail(
        val teamId: Int,
    ) : PlayerDetailEffect()
}
