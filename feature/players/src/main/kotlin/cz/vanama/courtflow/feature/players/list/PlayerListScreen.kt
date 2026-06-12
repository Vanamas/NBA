package cz.vanama.courtflow.feature.players.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
import cz.vanama.courtflow.core.designsystem.component.ErrorState
import cz.vanama.courtflow.core.designsystem.component.PlayerCard
import cz.vanama.courtflow.core.designsystem.component.TestTags
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.core.designsystem.util.PlaceholderImages
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.players.R
import cz.vanama.courtflow.feature.players.errorMessage
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel
import cz.vanama.courtflow.core.designsystem.R as DesignR

internal const val SEARCH_FIELD_TEST_TAG = "player_search_field"
internal const val REFRESH_ERROR_TEST_TAG = "refresh_error"

/**
 * Endlessly scrolling list of NBA players; tapping a row navigates to the
 * player detail via [onNavigateToPlayerDetail].
 */
@Composable
fun PlayerListScreen(
    onNavigateToPlayerDetail: (Int) -> Unit,
    onNavigateToTeams: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val players = uiState.players.collectAsLazyPagingItems()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is PlayerListEffect.NavigateToPlayerDetail -> onNavigateToPlayerDetail(effect.playerId)
                }
            }
        }
    }

    PlayerListScreen(
        players = players,
        searchQuery = uiState.searchQuery,
        onSearchQueryChanged = { query -> viewModel.onIntent(PlayerListIntent.OnSearchQueryChanged(query)) },
        onPlayerClick = { playerId -> viewModel.onIntent(PlayerListIntent.OnPlayerClicked(playerId)) },
        onNavigateToTeams = onNavigateToTeams,
        modifier = modifier,
    )
}

/**
 * Stateless player list screen with the [Scaffold] and top bar; rendered by
 * previews and free of any ViewModel wiring.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerListScreen(
    players: LazyPagingItems<Player>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onPlayerClick: (Int) -> Unit,
    onNavigateToTeams: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.player_list_title)) },
                actions = {
                    TextButton(onClick = onNavigateToTeams) {
                        Text(stringResource(R.string.player_list_teams_action))
                    }
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        PlayerListContent(
            players = players,
            searchQuery = searchQuery,
            onSearchQueryChanged = onSearchQueryChanged,
            onPlayerClick = onPlayerClick,
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Stateless content of the player list screen.
 *
 * @param players paginated players including their load states.
 * @param searchQuery current search text shown in the search field.
 * @param onSearchQueryChanged invoked on every change of the search text.
 * @param onPlayerClick invoked with the player id when a row is tapped.
 */
@Composable
internal fun PlayerListContent(
    players: LazyPagingItems<Player>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text(stringResource(R.string.player_list_search_hint)) },
            singleLine = true,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag(SEARCH_FIELD_TEST_TAG),
        )

        PlayerListItems(
            players = players,
            onPlayerClick = onPlayerClick,
        )
    }
}

/**
 * The list body below the search field: the lazy list with append states,
 * the initial-load spinner, or the first-load failure state.
 */
@Composable
private fun PlayerListItems(
    players: LazyPagingItems<Player>,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        val refreshState = players.loadState.refresh
        if (refreshState is LoadState.Error) {
            ErrorState(
                message = stringResource(R.string.player_list_refresh_error, errorMessage(refreshState.error)),
                onRetry = { players.retry() },
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .testTag(REFRESH_ERROR_TEST_TAG),
            )
        } else {
            PlayerLazyList(
                players = players,
                onPlayerClick = onPlayerClick,
            )

            if (refreshState is LoadState.Loading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .testTag(TestTags.LOADING_INDICATOR),
                )
            }
        }
    }
}

/** The lazy list of player cards followed by the append loading/error row. */
@Composable
private fun PlayerLazyList(
    players: LazyPagingItems<Player>,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(count = players.itemCount) { index ->
            val player = players[index]
            if (player != null) {
                PlayerCard(
                    firstName = player.firstName,
                    lastName = player.lastName,
                    position = player.position,
                    teamName = player.team.fullName,
                    imageUrl = PlaceholderImages.playerPortrait(player.id, size = 128),
                    onClick = { onPlayerClick(player.id) },
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }

        when (val loadState = players.loadState.append) {
            is LoadState.Loading -> {
                item { AppendLoading() }
            }
            is LoadState.Error -> {
                item {
                    AppendError(
                        error = loadState,
                        onRetry = { players.retry() },
                    )
                }
            }
            else -> {}
        }
    }
}

/** Inline next-page loading row. */
@Composable
private fun AppendLoading(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentWidth(Alignment.CenterHorizontally),
    )
}

/** Inline next-page failure row with a retry button. */
@Composable
private fun AppendError(
    error: LoadState.Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.player_list_append_error, errorMessage(error.error)),
            modifier = Modifier.padding(16.dp),
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(DesignR.string.retry))
        }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun PlayerListScreenPreview() {
    val team = Team(10, "GSW", "Golden State", "West", "Pacific", "Golden State Warriors", "Warriors")
    val players =
        listOf(
            Player(id = 19, firstName = "Stephen", lastName = "Curry", position = "G", team = team),
            Player(id = 20, firstName = "Klay", lastName = "Thompson", position = "G", team = team),
            Player(id = 21, firstName = "Draymond", lastName = "Green", position = "F", team = team),
        )

    CourtFlowTheme {
        PlayerListScreen(
            players = flowOf(PagingData.from(players)).collectAsLazyPagingItems(),
            searchQuery = "",
            onSearchQueryChanged = {},
            onPlayerClick = {},
            onNavigateToTeams = {},
        )
    }
}
