package cz.vanama.courtflow.feature.players.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
import cz.vanama.courtflow.core.designsystem.component.PlayerCardSkeleton
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
internal const val EMPTY_STATE_TEST_TAG = "player_list_empty_state"
internal const val PULL_TO_REFRESH_TEST_TAG = "player_pull_to_refresh"

private const val SKELETON_ITEM_COUNT = 7

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
        PlayerSearchField(
            query = searchQuery,
            onQueryChanged = onSearchQueryChanged,
        )

        PlayerListItems(
            players = players,
            searchQuery = searchQuery,
            onClearSearch = { onSearchQueryChanged("") },
            onPlayerClick = onPlayerClick,
        )
    }
}

/**
 * Single-line player search field with a clear (X) trailing icon — shown
 * only while [query] is non-empty — and a Search IME action that hides
 * the keyboard.
 */
@Composable
private fun PlayerSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text(stringResource(R.string.player_list_search_hint)) },
        singleLine = true,
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.player_list_clear_search),
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag(SEARCH_FIELD_TEST_TAG),
    )
}

/**
 * The list body below the search field: the lazy list with append states
 * wrapped in pull-to-refresh, the first-load skeleton placeholders, the
 * first-load failure state, or — when the refresh finished with zero items —
 * the empty state. The pull indicator only shows while refreshing an already
 * populated list; the very first load renders the skeleton column.
 */
@Composable
private fun PlayerListItems(
    players: LazyPagingItems<Player>,
    searchQuery: String,
    onClearSearch: () -> Unit,
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
                .testTag(PULL_TO_REFRESH_TEST_TAG),
    ) {
        when {
            refreshState is LoadState.Error -> {
                ErrorState(
                    message = stringResource(R.string.player_list_refresh_error, errorMessage(refreshState.error)),
                    onRetry = { players.retry() },
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .testTag(REFRESH_ERROR_TEST_TAG),
                )
            }
            refreshState is LoadState.Loading && players.itemCount == 0 -> {
                // First-load placeholder: a static column of shimmering skeletons
                // matching the real list's content padding and item spacing.
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    repeat(SKELETON_ITEM_COUNT) {
                        PlayerCardSkeleton(modifier = Modifier.padding(bottom = 8.dp))
                    }
                }
            }
            refreshState is LoadState.NotLoading && players.itemCount == 0 -> {
                EmptyPlayersState(
                    searchQuery = searchQuery,
                    onClearSearch = onClearSearch,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            else -> {
                PlayerLazyList(
                    players = players,
                    onPlayerClick = onPlayerClick,
                )
            }
        }
    }
}

/**
 * Centered empty state shown when the paging refresh finished with zero
 * players: either nothing matches [searchQuery] (offers a clear-search
 * action), or — with a blank query — the catalog itself is empty.
 */
@Composable
private fun EmptyPlayersState(
    searchQuery: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .padding(16.dp)
                .testTag(EMPTY_STATE_TEST_TAG),
    ) {
        Icon(
            imageVector = if (searchQuery.isBlank()) Icons.Filled.Person else Icons.Filled.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (searchQuery.isBlank()) {
            Text(
                text = stringResource(R.string.player_list_empty),
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = stringResource(R.string.player_list_empty_search, searchQuery),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onClearSearch) {
                Text(stringResource(R.string.player_list_clear_search))
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
