package cz.vanama.courtflow.feature.teams.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import cz.vanama.courtflow.core.designsystem.component.ErrorState
import cz.vanama.courtflow.core.designsystem.component.TeamCard
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.teams.R
import org.koin.androidx.compose.koinViewModel
import cz.vanama.courtflow.core.designsystem.R as DesignR

/**
 * List of all NBA teams; tapping a row navigates to the team detail via
 * [onNavigateToTeamDetail].
 */
@Composable
fun TeamListScreen(
    onNavigateToTeamDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
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
        onNavigateBack = onNavigateBack,
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
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.team_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(DesignR.string.navigate_back),
                        )
                    }
                },
            )
        },
        modifier = modifier.fillMaxSize(),
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
 * [onRetry] is invoked when the user retries after a load failure.
 */
@Composable
internal fun TeamListContent(
    state: TeamListState,
    onTeamClick: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .testTag("loading_indicator"),
                )
            }
            state.error != null -> {
                ErrorState(
                    message = state.error.ifBlank { stringResource(DesignR.string.error_unknown) },
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.teams, key = { it.id }) { team ->
                        TeamCard(
                            fullName = team.fullName,
                            conference = team.conference,
                            division = team.division,
                            onClick = { onTeamClick(team.id) },
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun TeamListScreenPreview() {
    CourtFlowTheme(dynamicColor = false) {
        TeamListScreen(
            state =
                TeamListState(
                    teams =
                        listOf(
                            Team(10, "GSW", "Golden State", "West", "Pacific", "Golden State Warriors", "Warriors"),
                            Team(14, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers"),
                        ),
                ),
            onTeamClick = {},
            onRetry = {},
            onNavigateBack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun TeamListScreenErrorPreview() {
    CourtFlowTheme(dynamicColor = false) {
        TeamListScreen(
            state = TeamListState(error = "Teams could not be loaded."),
            onTeamClick = {},
            onRetry = {},
            onNavigateBack = {},
        )
    }
}
