package cz.vanama.courtflow.feature.teams.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.designsystem.component.ConnectivityBanner
import cz.vanama.courtflow.core.designsystem.component.ErrorState
import cz.vanama.courtflow.core.designsystem.component.TeamCard
import cz.vanama.courtflow.core.designsystem.component.TeamCardSkeleton
import cz.vanama.courtflow.core.designsystem.component.TestTags
import cz.vanama.courtflow.core.designsystem.component.errorMessage
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.teams.R
import org.koin.androidx.compose.koinViewModel

/** Minimum width of one grid column; [GridCells.Adaptive] derives the column count from it. */
private val TEAM_CARD_MIN_WIDTH = 300.dp

private const val SKELETON_ITEM_COUNT = 8

/**
 * Grid of all NBA teams grouped by conference and division — the column
 * count adapts to the window width; tapping a card navigates to the team
 * detail via [onNavigateToTeamDetail]. A top-level destination reached from
 * the navigation suite, hence no back arrow of its own.
 */
@Composable
fun TeamListScreen(
    onNavigateToTeamDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeamListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is TeamListEffect.NavigateToTeamDetail -> onNavigateToTeamDetail(effect.teamId)
                }
            }
        }
    }

    TeamListScreen(
        state = uiState,
        onTeamClick = { teamId -> viewModel.onIntent(TeamListIntent.OnTeamClicked(teamId)) },
        onRetry = { viewModel.onIntent(TeamListIntent.Retry) },
        modifier = modifier,
    )
}

/**
 * Stateless team list screen with the [Scaffold] and top bar; rendered by
 * previews and free of any ViewModel wiring.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TeamListScreen(
    state: TeamListState,
    onTeamClick: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Hides on scroll down and reappears immediately on scroll up, freeing
    // vertical space for the grid while the user is consuming content.
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.team_list_title)) },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier =
            modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { padding ->
        TeamListContent(
            state = state,
            onTeamClick = onTeamClick,
            onRetry = onRetry,
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Stateless content of the team list screen rendered purely from [state];
 * [onRetry] is invoked when the user retries after a load failure. The grid
 * fits as many [TEAM_CARD_MIN_WIDTH]-wide columns as the window allows; the
 * section headers span the full line width.
 */
@Composable
internal fun TeamListContent(
    state: TeamListState,
    onTeamClick: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.isOffline,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            ConnectivityBanner(modifier = Modifier.testTag(TestTags.CONNECTIVITY_BANNER))
        }
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    // First-load placeholder: a static column of shimmering
                    // skeletons matching the grid's content padding.
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        repeat(SKELETON_ITEM_COUNT) {
                            TeamCardSkeleton(modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }
                }
                state.error != null -> {
                    ErrorState(
                        message = errorMessage(state.error),
                        onRetry = onRetry,
                        retryInSeconds = state.retryInSeconds,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = TEAM_CARD_MIN_WIDTH),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        state.sections.forEach { section ->
                            teamSection(section, state.favoriteIds, onTeamClick)
                        }
                    }
                }
            }
        }
    }
}

/** One full-width section header plus the team cards of a single [TeamSection]. */
private fun LazyGridScope.teamSection(
    section: TeamSection,
    favoriteIds: Set<Int>,
    onTeamClick: (Int) -> Unit,
) {
    item(
        key = "header:${section.conference}:${section.division}",
        span = { GridItemSpan(maxLineSpan) },
    ) {
        TeamSectionHeader(section)
    }
    items(section.teams, key = { it.id }) { team ->
        TeamCard(
            fullName = team.fullName,
            conference = team.conference,
            division = team.division,
            abbreviation = team.abbreviation,
            isFavorite = team.id in favoriteIds,
            onClick = { onTeamClick(team.id) },
        )
    }
}

/** Section title styled like RosterHeader. */
@Composable
private fun TeamSectionHeader(
    section: TeamSection,
    modifier: Modifier = Modifier,
) {
    Text(
        text = sectionTitle(section),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
    )
}

/** Maps raw API section keys to localized display text. */
@Composable
private fun sectionTitle(section: TeamSection): String =
    when {
        section.conference.isBlank() -> stringResource(R.string.team_list_section_other)
        section.division.isBlank() -> section.conference
        else -> stringResource(R.string.team_list_section_header, section.conference, section.division)
    }

private val previewCeltics = Team(2, "BOS", "Boston", "East", "Atlantic", "Boston Celtics", "Celtics")
private val previewWarriors = Team(10, "GSW", "Golden State", "West", "Pacific", "Golden State Warriors", "Warriors")
private val previewLakers = Team(14, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun TeamListScreenPreview() {
    CourtFlowTheme {
        TeamListScreen(
            state =
                TeamListState(
                    sections =
                        listOf(
                            TeamSection(
                                conference = "East",
                                division = "Atlantic",
                                teams = listOf(previewCeltics),
                            ),
                            TeamSection(
                                conference = "West",
                                division = "Pacific",
                                teams = listOf(previewWarriors, previewLakers),
                            ),
                        ),
                ),
            onTeamClick = {},
            onRetry = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun TeamListScreenErrorPreview() {
    CourtFlowTheme {
        TeamListScreen(
            state = TeamListState(error = DataErrorKind.SERVER),
            onTeamClick = {},
            onRetry = {},
        )
    }
}
