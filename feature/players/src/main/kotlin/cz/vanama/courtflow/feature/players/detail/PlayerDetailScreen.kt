package cz.vanama.courtflow.feature.players.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
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
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.players.R
import org.koin.androidx.compose.koinViewModel
import cz.vanama.courtflow.core.designsystem.R as DesignR

/**
 * Detail of a single player with all known attributes and a button
 * navigating to the player's team via [onNavigateToTeamDetail].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    playerId: Int,
    onNavigateToTeamDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(playerId) {
        viewModel.onIntent(PlayerDetailIntent.LoadPlayer(playerId))
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is PlayerDetailEffect.NavigateToTeamDetail -> onNavigateToTeamDetail(effect.teamId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.player_detail_title)) })
        },
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        PlayerDetailContent(
            state = uiState,
            onTeamClick = { teamId -> viewModel.onIntent(PlayerDetailIntent.OnTeamClicked(teamId)) },
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Stateless content of the player detail screen rendered purely from [state];
 * [onTeamClick] is invoked with the team id when the team button is tapped.
 */
@Composable
fun PlayerDetailContent(
    state: PlayerDetailState,
    onTeamClick: (Int) -> Unit,
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
            state.player?.let { player ->
                PlayerDetailBody(player = player, onTeamClick = onTeamClick)
            }
        }
    }
}

/** Scrollable column with the player's portrait, attributes and team button. */
@Composable
private fun PlayerDetailBody(
    player: Player,
    onTeamClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        AvatarImage(
            model = PlaceholderImages.playerPortrait(player.id),
            contentDescription = "${player.firstName} ${player.lastName}",
            loadingIcon = Icons.Filled.Person,
            modifier = Modifier.size(200.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${player.firstName} ${player.lastName}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.player_detail_position, player.position),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        PlayerAttribute(
            label = stringResource(R.string.player_attribute_height),
            value = player.height
        )
        PlayerAttribute(
            label = stringResource(R.string.player_attribute_weight),
            value = player.weight?.let { stringResource(R.string.player_attribute_weight_lbs, it) },
        )
        PlayerAttribute(
            label = stringResource(R.string.player_attribute_jersey_number),
            value = player.jerseyNumber
        )
        PlayerAttribute(
            label = stringResource(R.string.player_attribute_college),
            value = player.college
        )
        PlayerAttribute(
            label = stringResource(R.string.player_attribute_country),
            value = player.country
        )
        PlayerAttribute(
            label = stringResource(R.string.player_attribute_draft),
            value =
                player.draftYear?.let { year ->
                    listOfNotNull(
                        "$year",
                        player.draftRound?.let {
                            stringResource(
                                R.string.player_attribute_draft_round,
                                it
                            )
                        },
                        player.draftNumber?.let {
                            stringResource(
                                R.string.player_attribute_draft_pick,
                                it
                            )
                        },
                    ).joinToString(", ")
                },
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { onTeamClick(player.team.id) }) {
            Text(text = stringResource(R.string.player_detail_view_team, player.team.fullName))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerDetailContentPreview() {
    CourtFlowTheme(dynamicColor = false) {
        PlayerDetailContent(
            state =
                PlayerDetailState(
                    player =
                        Player(
                            id = 19,
                            firstName = "Stephen",
                            lastName = "Curry",
                            position = "G",
                            height = "6-2",
                            weight = "185",
                            jerseyNumber = "30",
                            college = "Davidson",
                            country = "USA",
                            draftYear = 2009,
                            draftRound = 1,
                            draftNumber = 7,
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
                ),
            onTeamClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerDetailContentLoadingPreview() {
    CourtFlowTheme(dynamicColor = false) {
        PlayerDetailContent(
            state = PlayerDetailState(isLoading = true),
            onTeamClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerDetailContentErrorPreview() {
    CourtFlowTheme(dynamicColor = false) {
        PlayerDetailContent(
            state = PlayerDetailState(error = "Player could not be loaded."),
            onTeamClick = {},
        )
    }
}

/** Single "label - value" row of the detail; renders nothing when [value] is `null`. */
@Composable
private fun PlayerAttribute(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    if (value == null) return
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(140.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
