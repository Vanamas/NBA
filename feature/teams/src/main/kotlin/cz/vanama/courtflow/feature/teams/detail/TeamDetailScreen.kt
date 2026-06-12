package cz.vanama.courtflow.feature.teams.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.vanama.courtflow.core.designsystem.component.AttributeRow
import cz.vanama.courtflow.core.designsystem.component.AvatarImage
import cz.vanama.courtflow.core.designsystem.component.Badge
import cz.vanama.courtflow.core.designsystem.component.BadgeTone
import cz.vanama.courtflow.core.designsystem.component.ErrorState
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.core.designsystem.util.PlaceholderImages
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.teams.R
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import cz.vanama.courtflow.core.designsystem.R as DesignR

/**
 * Detail of a single team with all information available from the API.
 */
@Composable
fun TeamDetailScreen(
    teamId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeamDetailViewModel = koinViewModel { parametersOf(teamId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeamDetailScreen(
        state = uiState,
        onRetry = { viewModel.onIntent(TeamDetailIntent.Retry) },
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
    onRetry: () -> Unit,
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
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        TeamDetailContent(
            state = state,
            onRetry = onRetry,
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Stateless content of the team detail screen rendered purely from [state];
 * [onRetry] is invoked when the user retries after a load failure.
 */
@Composable
internal fun TeamDetailContent(
    state: TeamDetailState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.testTag("loading_indicator"))
        } else if (state.error != null) {
            ErrorState(
                message = state.error.ifBlank { stringResource(DesignR.string.error_unknown) },
                onRetry = onRetry,
            )
        } else {
            state.team?.let { team ->
                TeamDetailBody(team = team)
            }
        }
    }
}

/** Column with the team emblem, name, abbreviation badge and attributes. */
@Composable
private fun TeamDetailBody(
    team: Team,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 28.dp),
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
    CourtFlowTheme(dynamicColor = false) {
        TeamDetailScreen(
            state =
                TeamDetailState(
                    team =
                        Team(
                            10,
                            "GSW",
                            "Golden State",
                            "West",
                            "Pacific",
                            "Golden State Warriors",
                            "Warriors",
                        ),
                ),
            onRetry = {},
            onNavigateBack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun TeamDetailScreenErrorPreview() {
    CourtFlowTheme(dynamicColor = false) {
        TeamDetailScreen(
            state = TeamDetailState(error = "Team could not be loaded."),
            onRetry = {},
            onNavigateBack = {},
        )
    }
}
