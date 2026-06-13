package cz.vanama.courtflow.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation destinations of the app; the Navigation 3 back stack in
 * [CourtFlowNavGraph] is a list of these keys, serialized across
 * configuration changes and process death.
 */
@Serializable
sealed interface Destination : NavKey {
    @Serializable
    data object PlayerList : Destination

    @Serializable
    data object TeamList : Destination

    @Serializable
    data object Settings : Destination

    @Serializable
    data class PlayerDetail(
        val playerId: Int,
    ) : Destination

    @Serializable
    data class TeamDetail(
        val teamId: Int,
    ) : Destination
}
