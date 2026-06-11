package cz.vanama.courtflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import cz.vanama.courtflow.feature.players.detail.PlayerDetailScreen
import cz.vanama.courtflow.feature.players.list.PlayerListScreen
import cz.vanama.courtflow.feature.teams.detail.TeamDetailScreen

/**
 * Root navigation of the app: a simple manual back stack of [Destination]
 * values rendered by a `when` over the top entry.
 */
@Composable
fun CourtFlowNavGraph() {
    val backStack = remember { mutableStateListOf<Destination>(Destination.PlayerList) }

    val currentDestination = backStack.last()

    when (currentDestination) {
        is Destination.PlayerList -> {
            PlayerListScreen(
                onNavigateToPlayerDetail = { playerId ->
                    backStack.add(Destination.PlayerDetail(playerId))
                },
            )
        }
        is Destination.PlayerDetail -> {
            PlayerDetailScreen(
                playerId = currentDestination.playerId,
                onNavigateToTeamDetail = { teamId ->
                    backStack.add(Destination.TeamDetail(teamId))
                },
            )
        }
        is Destination.TeamDetail -> {
            TeamDetailScreen(
                teamId = currentDestination.teamId,
            )
        }
    }
}
