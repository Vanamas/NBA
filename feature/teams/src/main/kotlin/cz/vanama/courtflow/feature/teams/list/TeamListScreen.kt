package cz.vanama.courtflow.feature.teams.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.teams.R
import org.koin.androidx.compose.koinViewModel
import cz.vanama.courtflow.core.designsystem.R as DesignR

/**
 * List of all NBA teams; tapping a row navigates to the team detail via
 * [onNavigateToTeamDetail].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamListScreen(
    onNavigateToTeamDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeamListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(TeamListIntent.LoadTeams)
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is TeamListEffect.NavigateToTeamDetail -> onNavigateToTeamDetail(effect.teamId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.team_list_title)) })
        },
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        TeamListContent(
            state = uiState,
            onTeamClick = { teamId -> viewModel.onIntent(TeamListIntent.OnTeamClicked(teamId)) },
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Stateless content of the team list screen rendered purely from [state].
 */
@Composable
fun TeamListContent(
    state: TeamListState,
    onTeamClick: (Int) -> Unit,
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
                Text(
                    text = state.error.ifBlank { stringResource(DesignR.string.error_unknown) },
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.teams, key = { it.id }) { team ->
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clickable { onTeamClick(team.id) },
                        ) {
                            Text(
                                text = team.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            )
                            Text(
                                text =
                                    stringResource(
                                        R.string.team_list_conference_division,
                                        team.conference,
                                        team.division,
                                    ),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamListContentPreview() {
    CourtFlowTheme(dynamicColor = false) {
        TeamListContent(
            state =
                TeamListState(
                    teams =
                        listOf(
                            Team(
                                10,
                                "GSW",
                                "Golden State",
                                "West",
                                "Pacific",
                                "Golden State Warriors",
                                "Warriors"
                            ),
                            Team(
                                14,
                                "LAL",
                                "Los Angeles",
                                "West",
                                "Pacific",
                                "Los Angeles Lakers",
                                "Lakers"
                            ),
                        ),
                ),
            onTeamClick = {},
        )
    }
}
