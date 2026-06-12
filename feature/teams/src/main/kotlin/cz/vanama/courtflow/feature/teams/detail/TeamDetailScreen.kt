package cz.vanama.courtflow.feature.teams.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.vanama.courtflow.core.designsystem.component.AvatarImage
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.core.designsystem.util.PlaceholderImages
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.teams.R
import org.koin.androidx.compose.koinViewModel
import cz.vanama.courtflow.core.designsystem.R as DesignR

/**
 * Detail of a single team with all information available from the API.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamId: Int,
    modifier: Modifier = Modifier,
    viewModel: TeamDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(teamId) {
        viewModel.onIntent(TeamDetailIntent.LoadTeam(teamId))
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.team_detail_title)) })
        },
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        TeamDetailContent(
            state = uiState,
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Stateless content of the team detail screen rendered purely from [state].
 */
@Composable
fun TeamDetailContent(
    state: TeamDetailState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.testTag("loading_indicator"))
        } else if (state.error != null) {
            Text(text = state.error.ifBlank { stringResource(DesignR.string.error_unknown) })
        } else {
            state.team?.let { team ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp),
                ) {
                    AvatarImage(
                        model = PlaceholderImages.teamEmblem(team.id),
                        contentDescription = team.fullName,
                        loadingIcon = Icons.Filled.Groups,
                        modifier = Modifier.size(200.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = team.fullName,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.team_detail_abbreviation, team.abbreviation),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.team_detail_city, team.city),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.team_detail_conference, team.conference),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.team_detail_division, team.division),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamDetailContentPreview() {
    CourtFlowTheme(dynamicColor = false) {
        TeamDetailContent(
            state =
                TeamDetailState(
                    team = Team(
                        10,
                        "GSW",
                        "Golden State",
                        "West",
                        "Pacific",
                        "Golden State Warriors",
                        "Warriors"
                    ),
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamDetailContentErrorPreview() {
    CourtFlowTheme(dynamicColor = false) {
        TeamDetailContent(state = TeamDetailState(error = "Team could not be loaded."))
    }
}
