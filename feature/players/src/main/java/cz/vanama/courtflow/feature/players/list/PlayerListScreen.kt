package cz.vanama.courtflow.feature.players.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import cz.vanama.courtflow.core.designsystem.component.PlayerCard
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.core.designsystem.util.PlaceholderImages
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel

/**
 * Endlessly scrolling list of NBA players; tapping a row navigates to the
 * player detail via [onNavigateToPlayerDetail].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerListScreen(
    onNavigateToPlayerDetail: (Int) -> Unit,
    onNavigateToTeams: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val players = uiState.players?.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.onIntent(PlayerListIntent.LoadPlayers)
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is PlayerListEffect.NavigateToPlayerDetail -> onNavigateToPlayerDetail(effect.playerId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NBA Players") },
                actions = {
                    TextButton(onClick = onNavigateToTeams) {
                        Text("Teams")
                    }
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        PlayerListContent(
            players = players,
            searchQuery = uiState.searchQuery,
            onSearchQueryChanged = { query -> viewModel.onIntent(PlayerListIntent.OnSearchQueryChanged(query)) },
            onPlayerClick = { playerId -> viewModel.onIntent(PlayerListIntent.OnPlayerClicked(playerId)) },
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Stateless content of the player list screen.
 *
 * @param players paginated players, `null` while the stream is not started yet.
 * @param searchQuery current search text shown in the search field.
 * @param onSearchQueryChanged invoked on every change of the search text.
 * @param onPlayerClick invoked with the player id when a row is tapped.
 */
@Composable
fun PlayerListContent(
    players: LazyPagingItems<Player>?,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search players") },
            singleLine = true,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("player_search_field"),
        )

        PlayerListItems(
            players = players,
            onPlayerClick = onPlayerClick,
        )
    }
}

/**
 * The list body below the search field: loading indicator, the lazy list
 * with append states, or the initial-load spinner.
 */
@Composable
private fun PlayerListItems(
    players: LazyPagingItems<Player>?,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (players == null) {
            CircularProgressIndicator(
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .testTag("loading_indicator"),
            )
        } else if (players.loadState.refresh is LoadState.Error) {
            val refreshError = players.loadState.refresh as LoadState.Error
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .testTag("refresh_error"),
            ) {
                Text(text = "Failed to load players: ${refreshError.error.message}")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { players.retry() }) {
                    Text("Retry")
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize(),
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
                        item {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally),
                            )
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "Error loading more players: ${loadState.error.message}",
                                    modifier = Modifier.padding(16.dp),
                                )
                                TextButton(onClick = { players.retry() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }

            if (players.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun PlayerListContentPreview() {
    val team = Team(10, "GSW", "Golden State", "West", "Pacific", "Golden State Warriors", "Warriors")
    val players =
        listOf(
            Player(id = 19, firstName = "Stephen", lastName = "Curry", position = "G", team = team),
            Player(id = 20, firstName = "Klay", lastName = "Thompson", position = "G", team = team),
            Player(id = 21, firstName = "Draymond", lastName = "Green", position = "F", team = team),
        )

    CourtFlowTheme(dynamicColor = false) {
        PlayerListContent(
            players = flowOf(PagingData.from(players)).collectAsLazyPagingItems(),
            searchQuery = "",
            onSearchQueryChanged = {},
            onPlayerClick = {},
        )
    }
}
