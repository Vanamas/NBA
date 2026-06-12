package cz.vanama.courtflow.feature.teams.detail

import android.content.ClipDescription
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.designsystem.component.AttributeRow
import cz.vanama.courtflow.core.designsystem.component.AvatarImage
import cz.vanama.courtflow.core.designsystem.component.Badge
import cz.vanama.courtflow.core.designsystem.component.BadgeTone
import cz.vanama.courtflow.core.designsystem.component.ErrorState
import cz.vanama.courtflow.core.designsystem.component.OfflineBanner
import cz.vanama.courtflow.core.designsystem.component.TestTags
import cz.vanama.courtflow.core.designsystem.component.errorMessage
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.core.designsystem.util.PlaceholderImages
import cz.vanama.courtflow.domain.model.Game
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.teams.R
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import cz.vanama.courtflow.core.designsystem.R as DesignR

internal const val TEAM_DETAIL_LIST_TEST_TAG = "team_detail_list"
internal const val TEAM_ROSTER_PULL_TO_REFRESH_TEST_TAG = "team_roster_pull_to_refresh"
internal const val TEAM_ROSTER_OFFLINE_BANNER_TEST_TAG = "team_roster_offline_banner"

/**
 * Detail of a single team with all information available from the API and
 * the team's paginated player roster; tapping a roster row navigates to the
 * player detail via [onNavigateToPlayerDetail].
 */
@Composable
fun TeamDetailScreen(
    teamId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToPlayerDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeamDetailViewModel = koinViewModel { parametersOf(teamId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val players = uiState.players.collectAsLazyPagingItems()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is TeamDetailEffect.NavigateToPlayerDetail -> onNavigateToPlayerDetail(effect.playerId)
                    is TeamDetailEffect.Share -> {
                        val text = context.getString(R.string.share_team_text, effect.team.fullName, effect.team.id)
                        val sendIntent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = ClipDescription.MIMETYPE_TEXT_PLAIN
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }
                }
            }
        }
    }

    TeamDetailScreen(
        state = uiState,
        players = players,
        onRetry = { viewModel.onIntent(TeamDetailIntent.Retry) },
        onPlayerClick = { playerId -> viewModel.onIntent(TeamDetailIntent.OnPlayerClicked(playerId)) },
        onShare = { viewModel.onIntent(TeamDetailIntent.OnShareClicked) },
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * Stateless team detail screen with the [Scaffold] and top bar; rendered by
 * previews and free of any ViewModel wiring.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TeamDetailScreen(
    state: TeamDetailState,
    players: LazyPagingItems<Player>,
    onRetry: () -> Unit,
    onPlayerClick: (Int) -> Unit,
    onShare: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.team_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(DesignR.string.navigate_back),
                        )
                    }
                },
                actions = {
                    if (state.team != null) {
                        IconButton(onClick = onShare) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(R.string.share_team),
                            )
                        }
                    }
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        TeamDetailContent(
            state = state,
            players = players,
            onRetry = onRetry,
            onPlayerClick = onPlayerClick,
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Stateless content of the team detail screen rendered purely from [state]
 * and the paginated roster in [players]; [onRetry] is invoked when the user
 * retries after a load failure, [onPlayerClick] when a roster row is tapped.
 */
@Composable
internal fun TeamDetailContent(
    state: TeamDetailState,
    players: LazyPagingItems<Player>,
    onRetry: () -> Unit,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.testTag(TestTags.LOADING_INDICATOR))
        } else if (state.error != null) {
            ErrorState(
                message = errorMessage(state.error),
                onRetry = onRetry,
                retryInSeconds = state.retryInSeconds,
            )
        } else {
            state.team?.let { team ->
                TeamDetailBody(
                    team = team,
                    recentGames = state.recentGames,
                    players = players,
                    onPlayerClick = onPlayerClick,
                )
            }
        }
    }
}

/**
 * Scrollable body: the team header followed by the player roster, wrapped in
 * pull-to-refresh. The pull indicator only shows while refreshing an already
 * populated roster; the very first roster load keeps the inline spinner. A
 * refresh failure with cached roster rows keeps them visible behind an
 * [OfflineBanner] instead of replacing them with the full-width error.
 */
@Composable
private fun TeamDetailBody(
    team: Team,
    recentGames: List<Game>,
    players: LazyPagingItems<Player>,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshState = players.loadState.refresh
    PullToRefreshBox(
        isRefreshing = refreshState is LoadState.Loading && players.itemCount > 0,
        onRefresh = { players.refresh() },
        modifier =
            modifier
                .fillMaxSize()
                .testTag(TEAM_ROSTER_PULL_TO_REFRESH_TEST_TAG),
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxSize()
                    .testTag(TEAM_DETAIL_LIST_TEST_TAG),
        ) {
            item { TeamHeader(team = team) }

            recentGamesSection(recentGames)

            when {
                refreshState is LoadState.Error && players.itemCount == 0 ->
                    item {
                        RosterRefreshError(
                            error = refreshState,
                            onRetry = { players.retry() },
                        )
                    }
                else -> {
                    if (refreshState is LoadState.Error) {
                        // Refresh failed but cached roster rows are available: keep
                        // them on screen and surface the failure as an inline banner.
                        item {
                            OfflineBanner(
                                message = stringResource(R.string.team_roster_offline_banner),
                                onRetry = { players.retry() },
                                modifier = Modifier.testTag(TEAM_ROSTER_OFFLINE_BANNER_TEST_TAG),
                            )
                        }
                    }
                    rosterItems(team = team, players = players, onPlayerClick = onPlayerClick)
                    if (refreshState is LoadState.Loading && players.itemCount == 0) {
                        item { RosterLoading() }
                    }
                }
            }
        }
    }
}

/** The "Recent games" block; renders nothing when [games] is empty (off-season or failed load). */
private fun LazyListScope.recentGamesSection(games: List<Game>) {
    if (games.isEmpty()) return
    item { SectionHeader(text = stringResource(R.string.team_detail_recent_games)) }
    items(games, key = { it.id }) { game ->
        RecentGameRow(game = game)
    }
}

/** One compact score line: game date, home abbreviation, score, visitor abbreviation. */
@Composable
private fun RecentGameRow(
    game: Game,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
    ) {
        Text(
            text = game.date,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = game.homeTeam.abbreviation,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.game_score, game.homeTeamScore, game.visitorTeamScore),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = game.visitorTeam.abbreviation,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** The team emblem, name, abbreviation badge and attribute rows. */
@Composable
private fun TeamHeader(
    team: Team,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 12.dp),
    ) {
        AvatarImage(
            model = PlaceholderImages.teamEmblem(team.id),
            contentDescription = team.fullName,
            loadingIcon = Icons.Filled.Groups,
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(160.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = team.fullName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Badge(
            text = team.abbreviation,
            tone = BadgeTone.Primary,
            textStyle = MaterialTheme.typography.labelMedium,
            minHeight = 26.dp,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier =
                Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth(),
        ) {
            AttributeRow(
                label = stringResource(R.string.team_attribute_city),
                value = team.city,
            )
            AttributeRow(
                label = stringResource(R.string.team_attribute_conference),
                value = team.conference,
            )
            AttributeRow(
                label = stringResource(R.string.team_attribute_division),
                value = team.division,
            )
        }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun TeamDetailScreenPreview() {
    val team = Team(10, "GSW", "Golden State", "West", "Pacific", "Golden State Warriors", "Warriors")
    val players =
        listOf(
            Player(id = 19, firstName = "Stephen", lastName = "Curry", position = "G", team = team),
            Player(id = 21, firstName = "Draymond", lastName = "Green", position = "F", team = team),
        )
    val lakers = Team(14, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")
    val games =
        listOf(
            Game(
                id = 1,
                date = "2026-06-10",
                homeTeam = team,
                homeTeamScore = 112,
                visitorTeam = lakers,
                visitorTeamScore = 99,
            ),
        )

    CourtFlowTheme {
        TeamDetailScreen(
            state = TeamDetailState(team = team, recentGames = games),
            players = flowOf(PagingData.from(players)).collectAsLazyPagingItems(),
            onRetry = {},
            onPlayerClick = {},
            onShare = {},
            onNavigateBack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun TeamDetailScreenErrorPreview() {
    CourtFlowTheme {
        TeamDetailScreen(
            state = TeamDetailState(error = DataErrorKind.SERVER),
            players = flowOf(PagingData.empty<Player>()).collectAsLazyPagingItems(),
            onRetry = {},
            onPlayerClick = {},
            onShare = {},
            onNavigateBack = {},
        )
    }
}
