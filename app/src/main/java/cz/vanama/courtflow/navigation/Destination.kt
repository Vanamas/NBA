package cz.vanama.courtflow.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation destinations of the app; the back stack in
 * [CourtFlowNavGraph] is a list of these values.
 */
@Serializable
sealed interface Destination {
    @Serializable
    data object PlayerList : Destination

    @Serializable
    data class PlayerDetail(
        val playerId: Int,
    ) : Destination

    @Serializable
    data class TeamDetail(
        val teamId: Int,
    ) : Destination
}
