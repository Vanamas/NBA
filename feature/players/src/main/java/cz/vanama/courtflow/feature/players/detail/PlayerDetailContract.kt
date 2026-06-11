package cz.vanama.courtflow.feature.players.detail

import cz.vanama.courtflow.domain.model.Player

/** UI state of the player detail screen. */
data class PlayerDetailState(
    val isLoading: Boolean = false,
    val player: Player? = null,
    val error: String? = null
)

/** User actions of the player detail screen. */
sealed class PlayerDetailIntent {
    data class LoadPlayer(val playerId: Int) : PlayerDetailIntent()
    data class OnTeamClicked(val teamId: Int) : PlayerDetailIntent()
}

/** One-shot events emitted by [PlayerDetailViewModel]. */
sealed class PlayerDetailEffect {
    data class NavigateToTeamDetail(val teamId: Int) : PlayerDetailEffect()
}
