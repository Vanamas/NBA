package cz.vanama.courtflow.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import cz.vanama.courtflow.R
import cz.vanama.courtflow.feature.players.detail.PlayerDetailScreen
import cz.vanama.courtflow.feature.players.list.PlayerListScreen
import cz.vanama.courtflow.feature.settings.SettingsScreen
import cz.vanama.courtflow.feature.teams.detail.TeamDetailScreen
import cz.vanama.courtflow.feature.teams.list.TeamListScreen
import cz.vanama.courtflow.core.designsystem.R as DesignR

/**
 * Root navigation of the app: a [NavigationSuiteScaffold] hosting the two
 * top-level destinations (Players, Teams) around a Navigation 3 [NavDisplay]
 * over a saveable back stack of [Destination] keys. The navigation area
 * renders as a bottom bar on compact windows and as a navigation rail on
 * larger ones. The stack survives configuration changes and process death;
 * each entry keeps its own saved state (scroll positions) and
 * ViewModelStore, and the system back gesture pops with predictive-back
 * animation. On window widths that fit two panes (tablets, unfolded
 * foldables) the list and detail screens render side by side via
 * [ListDetailSceneStrategy].
 */
@Suppress("SpreadOperator") // rememberNavBackStack only offers a vararg overload; the list is tiny.
@Composable
fun CourtFlowNavGraph(
    modifier: Modifier = Modifier,
    initialBackStack: List<Destination> = listOf(Destination.PlayerList),
) {
    val backStack = rememberNavBackStack(*initialBackStack.toTypedArray())

    NavigationSuiteScaffold(
        navigationItems = { CourtFlowNavigationItems(backStack) },
        modifier = modifier,
    ) {
        CourtFlowNavDisplay(backStack = backStack)
    }
}

/**
 * The two top-level navigation items. The selected one is derived from the
 * top of [backStack] — no extra selection state to keep in sync. The stack
 * always stays rooted at [Destination.PlayerList], so system back from the
 * Teams section returns to the Players section (the home destination).
 */
@Composable
private fun CourtFlowNavigationItems(backStack: NavBackStack<NavKey>) {
    val topDestination = backStack.lastOrNull()
    val teamsSelected = topDestination is Destination.TeamList || topDestination is Destination.TeamDetail
    val settingsSelected = topDestination is Destination.Settings
    val popToRoot: () -> Unit = { while (backStack.size > 1) backStack.removeLastOrNull() }

    NavigationSuiteItem(
        selected = !teamsSelected && !settingsSelected,
        onClick = popToRoot,
        icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null) },
        label = { Text(stringResource(R.string.nav_players)) },
    )
    NavigationSuiteItem(
        selected = teamsSelected,
        onClick = {
            if (topDestination !is Destination.TeamList) {
                popToRoot()
                backStack.add(Destination.TeamList)
            }
        },
        icon = { Icon(imageVector = Icons.Filled.Groups, contentDescription = null) },
        label = { Text(stringResource(R.string.nav_teams)) },
    )
    NavigationSuiteItem(
        selected = settingsSelected,
        onClick = {
            if (topDestination !is Destination.Settings) {
                popToRoot()
                backStack.add(Destination.Settings)
            }
        },
        icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = null) },
        label = { Text(stringResource(R.string.nav_settings)) },
    )
}

/** The [NavDisplay] with all destination entries and the list-detail scene strategy. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun CourtFlowNavDisplay(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    val navigateBack: () -> Unit = { backStack.removeLastOrNull() }
    // ListDetailSceneStrategy switches to two panes from the expanded width
    // breakpoint up; the detail screens then sit right next to their list
    // and must not offer a back arrow of their own.
    val showBackButton =
        !currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    NavDisplay(
        backStack = backStack,
        onBack = navigateBack,
        sceneStrategies = listOf(rememberListDetailSceneStrategy()),
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider = courtFlowEntryProvider(backStack, navigateBack, showBackButton),
        modifier = modifier,
    )
}

/** Scene key grouping the player list and player detail into one list-detail scaffold. */
private const val PLAYERS_SCENE_KEY = "players"

/** Scene key grouping the team list and team detail into one list-detail scaffold. */
private const val TEAMS_SCENE_KEY = "teams"

/**
 * Builds the entry for each [Destination], with list/detail pane metadata
 * where applicable. The Players and Teams sections use distinct scene keys so
 * each section forms its own list-detail scaffold — with a shared key the
 * strategy would pull both lists into one scene and pick the wrong
 * detail placeholder.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun courtFlowEntryProvider(
    backStack: NavBackStack<NavKey>,
    navigateBack: () -> Unit,
    showBackButton: Boolean,
): (NavKey) -> NavEntry<NavKey> =
    entryProvider {
        entry<Destination.PlayerList>(
            metadata =
                ListDetailSceneStrategy.listPane(
                    sceneKey = PLAYERS_SCENE_KEY,
                    detailPlaceholder = {
                        PanePlaceholder(stringResource(DesignR.string.select_player_placeholder))
                    },
                ),
        ) {
            PlayerListScreen(
                onNavigateToPlayerDetail = { playerId -> backStack.add(Destination.PlayerDetail(playerId)) },
            )
        }
        entry<Destination.TeamList>(
            metadata =
                ListDetailSceneStrategy.listPane(
                    sceneKey = TEAMS_SCENE_KEY,
                    detailPlaceholder = {
                        PanePlaceholder(stringResource(DesignR.string.select_team_placeholder))
                    },
                ),
        ) {
            TeamListScreen(
                onNavigateToTeamDetail = { teamId -> backStack.add(Destination.TeamDetail(teamId)) },
            )
        }
        entry<Destination.PlayerDetail>(
            metadata = ListDetailSceneStrategy.detailPane(sceneKey = PLAYERS_SCENE_KEY),
        ) { destination ->
            PlayerDetailScreen(
                playerId = destination.playerId,
                onNavigateToTeamDetail = { teamId -> backStack.add(Destination.TeamDetail(teamId)) },
                onNavigateBack = navigateBack,
                showBackButton = showBackButton,
            )
        }
        entry<Destination.TeamDetail>(
            metadata = ListDetailSceneStrategy.detailPane(sceneKey = TEAMS_SCENE_KEY),
        ) { destination ->
            // Reached from a player detail there is no team list in the stack,
            // so even a wide window renders this detail alone — keep its own
            // back affordance in that case.
            val standaloneDetail = backStack.none { it is Destination.TeamList }
            TeamDetailScreen(
                teamId = destination.teamId,
                onNavigateBack = navigateBack,
                onNavigateToPlayerDetail = { playerId -> backStack.add(Destination.PlayerDetail(playerId)) },
                showBackButton = showBackButton || standaloneDetail,
            )
        }
        entry<Destination.Settings> {
            SettingsScreen()
        }
    }

/** Detail-pane placeholder shown on wide windows before a list item is selected. */
@Composable
private fun PanePlaceholder(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text)
    }
}
