package cz.vanama.courtflow.feature.teams.detail

import android.content.ClipDescription
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import cz.vanama.courtflow.core.designsystem.component.AttributeRow
import cz.vanama.courtflow.core.designsystem.component.AvatarImage
import cz.vanama.courtflow.core.designsystem.component.Badge
import cz.vanama.courtflow.core.designsystem.component.BadgeTone
import cz.vanama.courtflow.core.designsystem.component.ErrorState
import cz.vanama.courtflow.core.designsystem.component.PlayerCard
import cz.vanama.courtflow.core.designsystem.component.TestTags
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.core.designsystem.util.PlaceholderImages
import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.teams.R
import cz.vanama.courtflow.feature.teams.errorMessage
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import cz.vanama.courtflow.core.designsystem.R as DesignR

internal const val TEAM_DETAIL_LIST_TEST_TAG = "team_detail_list"
internal const val ROSTER_REFRESH_ERROR_TEST_TAG = "roster_refresh_error"
internal const val ROSTER_LOADING_TEST_TAG = "roster_loading"

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
            )
        } else {
            state.team?.let { team ->
                TeamDetailBody(
                    team = team,
                    players = players,
                    onPlayerClick = onPlayerClick,
                )
            }
        }
    }
}

/** Scrollable body: the team header followed by the player roster. */
@Composable
private fun TeamDetailBody(
    team: Team,
    players: LazyPagingItems<Player>,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .fillMaxSize()
                .testTag(TEAM_DETAIL_LIST_TEST_TAG),
    ) {
        item { TeamHeader(team = team) }

        when (val refreshState = players.loadState.refresh) {
            is LoadState.Error ->
                item {
                    RosterRefreshError(
                        error = refreshState,
                        onRetry = { players.retry() },
                    )
                }
            else -> rosterItems(team = team, players = players, onPlayerClick = onPlayerClick)
        }
    }
}

/** Roster rows: section header, player cards and the trailing append state. */
private fun LazyListScope.rosterItems(
    team: Team,
    players: LazyPagingItems<Player>,
    onPlayerClick: (Int) -> Unit,
) {
    if (players.itemCount > 0) {
        item { RosterHeader() }
    }

    items(count = players.itemCount) { index ->
        val player = players[index]
        if (player != null) {
            PlayerCard(
                firstName = player.firstName,
                lastName = player.lastName,
                position = player.position,
                teamName = team.fullName,
                imageUrl = PlaceholderImages.playerPortrait(player.id, size = 128),
                onClick = { onPlayerClick(player.id) },
                modifier =
                    Modifier
                        .widthIn(max = 360.dp)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 8.dp),
            )
        }
    }

    if (players.loadState.append is LoadState.Loading) {
        item { RosterAppendLoading() }
    }
}

/** Full-width roster failure state for the first roster page. */
@Composable
private fun RosterRefreshError(
    error: LoadState.Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ErrorState(
        message = stringResource(R.string.team_roster_refresh_error, errorMessage(error.error)),
        onRetry = onRetry,
        modifier = modifier.testTag(ROSTER_REFRESH_ERROR_TEST_TAG),
    )
}

/** Inline next-page loading row. */
@Composable
private fun RosterAppendLoading(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentWidth(Alignment.CenterHorizontally),
    )
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

/** Section title above the roster list. */
@Composable
private fun RosterHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.team_detail_roster),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier =
            modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 8.dp),
    )
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

    CourtFlowTheme {
        TeamDetailScreen(
            state = TeamDetailState(team = team),
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
