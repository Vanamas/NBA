package cz.vanama.courtflow.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import cz.vanama.courtflow.feature.players.detail.PlayerDetailScreen
import cz.vanama.courtflow.feature.players.list.PlayerListScreen
import cz.vanama.courtflow.feature.teams.detail.TeamDetailScreen
import cz.vanama.courtflow.feature.teams.list.TeamListScreen

/**
 * Root navigation of the app: a simple manual back stack of [Destination]
 * values rendered by a `when` over the top entry. Both the system back
 * gesture and the top bar back arrows pop the stack; on the root
 * destination the system back closes the activity as usual.
 */
@Composable
fun CourtFlowNavGraph() {
    val backStack = remember { mutableStateListOf<Destination>(Destination.PlayerList) }

    val navigateBack: () -> Unit = { backStack.removeAt(backStack.lastIndex) }

    BackHandler(enabled = backStack.size > 1, onBack = navigateBack)

    when (val currentDestination = backStack.last()) {
        is Destination.PlayerList -> {
            PlayerListScreen(
                onNavigateToPlayerDetail = { playerId ->
                    backStack.add(Destination.PlayerDetail(playerId))
                },
                onNavigateToTeams = {
                    backStack.add(Destination.TeamList)
                },
            )
        }
        is Destination.TeamList -> {
            TeamListScreen(
                onNavigateToTeamDetail = { teamId ->
                    backStack.add(Destination.TeamDetail(teamId))
                },
                onNavigateBack = navigateBack,
            )
        }
        is Destination.PlayerDetail -> {
            PlayerDetailScreen(
                playerId = currentDestination.playerId,
                onNavigateToTeamDetail = { teamId ->
                    backStack.add(Destination.TeamDetail(teamId))
                },
                onNavigateBack = navigateBack,
            )
        }
        is Destination.TeamDetail -> {
            TeamDetailScreen(
                teamId = currentDestination.teamId,
                onNavigateBack = navigateBack,
            )
        }
    }
}
