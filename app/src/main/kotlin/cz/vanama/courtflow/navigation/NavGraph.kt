package cz.vanama.courtflow.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import cz.vanama.courtflow.feature.players.detail.PlayerDetailScreen
import cz.vanama.courtflow.feature.players.list.PlayerListScreen
import cz.vanama.courtflow.feature.teams.detail.TeamDetailScreen
import cz.vanama.courtflow.feature.teams.list.TeamListScreen
import cz.vanama.courtflow.core.designsystem.R as DesignR

/**
 * Root navigation of the app: a Navigation 3 [NavDisplay] over a saveable
 * back stack of [Destination] keys. The stack survives configuration
 * changes and process death; each entry keeps its own saved state
 * (scroll positions) and ViewModelStore, and the system back gesture
 * pops with predictive-back animation. On window widths that fit two
 * panes (tablets, unfolded foldables) the player list and player detail
 * render side by side via [ListDetailSceneStrategy].
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun CourtFlowNavGraph(
    modifier: Modifier = Modifier,
    initialBackStack: List<Destination> = listOf(Destination.PlayerList),
) {
    val backStack = rememberNavBackStack(*initialBackStack.toTypedArray())
    val navigateBack: () -> Unit = { backStack.removeLastOrNull() }

    NavDisplay(
        backStack = backStack,
        onBack = navigateBack,
        sceneStrategies = listOf(rememberListDetailSceneStrategy()),
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<Destination.PlayerList>(
                    metadata =
                        ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { SelectPlayerPlaceholder() },
                        ),
                ) {
                    PlayerListScreen(
                        onNavigateToPlayerDetail = { playerId -> backStack.add(Destination.PlayerDetail(playerId)) },
                        onNavigateToTeams = { backStack.add(Destination.TeamList) },
                    )
                }
                entry<Destination.TeamList> {
                    TeamListScreen(
                        onNavigateToTeamDetail = { teamId -> backStack.add(Destination.TeamDetail(teamId)) },
                        onNavigateBack = navigateBack,
                    )
                }
                entry<Destination.PlayerDetail>(
                    metadata = ListDetailSceneStrategy.detailPane(),
                ) { destination ->
                    PlayerDetailScreen(
                        playerId = destination.playerId,
                        onNavigateToTeamDetail = { teamId -> backStack.add(Destination.TeamDetail(teamId)) },
                        onNavigateBack = navigateBack,
                    )
                }
                entry<Destination.TeamDetail> { destination ->
                    TeamDetailScreen(
                        teamId = destination.teamId,
                        onNavigateBack = navigateBack,
                        onNavigateToPlayerDetail = { playerId -> backStack.add(Destination.PlayerDetail(playerId)) },
                    )
                }
            },
        modifier = modifier,
    )
}

/** Detail-pane placeholder shown on wide windows before a player is selected. */
@Composable
private fun SelectPlayerPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(DesignR.string.select_player_placeholder))
    }
}
