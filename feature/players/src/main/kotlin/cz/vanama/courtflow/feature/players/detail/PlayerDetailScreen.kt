package cz.vanama.courtflow.feature.players.detail

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import cz.vanama.courtflow.core.designsystem.component.AttributeRow
import cz.vanama.courtflow.core.designsystem.component.AvatarImage
import cz.vanama.courtflow.core.designsystem.component.Badge
import cz.vanama.courtflow.core.designsystem.component.ErrorState
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.core.designsystem.util.PlaceholderImages
import cz.vanama.courtflow.core.designsystem.util.positionLabel
import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.players.R
import cz.vanama.courtflow.feature.players.errorMessage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import cz.vanama.courtflow.core.designsystem.R as DesignR

/**
 * Detail of a single player with all known attributes and a button
 * navigating to the player's team via [onNavigateToTeamDetail].
 */
@Composable
fun PlayerDetailScreen(
    playerId: Int,
    onNavigateToTeamDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerDetailViewModel = koinViewModel { parametersOf(playerId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is PlayerDetailEffect.NavigateToTeamDetail -> onNavigateToTeamDetail(effect.teamId)
                    is PlayerDetailEffect.Share -> {
                        val text =
                            context.getString(
                                R.string.share_player_text,
                                effect.player.firstName,
                                effect.player.lastName,
                                effect.player.id,
                            )
                        val sendIntent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }
                }
            }
        }
    }

    PlayerDetailScreen(
        state = uiState,
        onTeamClick = { teamId -> viewModel.onIntent(PlayerDetailIntent.OnTeamClicked(teamId)) },
        onRetry = { viewModel.onIntent(PlayerDetailIntent.Retry) },
        onShare = { viewModel.onIntent(PlayerDetailIntent.OnShareClicked) },
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * Stateless player detail screen with the [Scaffold] and top bar; rendered by
 * previews and free of any ViewModel wiring.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerDetailScreen(
    state: PlayerDetailState,
    onTeamClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onShare: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.player_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(DesignR.string.navigate_back),
                        )
                    }
                },
                actions = {
                    if (state.player != null) {
                        IconButton(onClick = onShare) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(R.string.share_player),
                            )
                        }
                    }
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        PlayerDetailContent(
            state = state,
            onTeamClick = onTeamClick,
            onRetry = onRetry,
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Stateless content of the player detail screen rendered purely from [state];
 * [onTeamClick] is invoked with the team id when the team button is tapped and
 * [onRetry] when the user retries after a load failure.
 */
@Composable
internal fun PlayerDetailContent(
    state: PlayerDetailState,
    onTeamClick: (Int) -> Unit,
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
                message = errorMessage(state.error),
                onRetry = onRetry,
            )
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 28.dp),
    ) {
        AvatarImage(
            model = PlaceholderImages.playerPortrait(player.id),
            contentDescription = "${player.firstName} ${player.lastName}",
            loadingIcon = Icons.Filled.Person,
            modifier = Modifier.size(160.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${player.firstName} ${player.lastName}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (player.position.isNotBlank()) {
                Badge(text = player.position)
            }
            Text(
                text = positionLabel(player.position),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        PlayerAttributes(
            player = player,
            modifier =
                Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(28.dp))
        ViewTeamButton(
            teamName = player.team.fullName,
            onClick = { onTeamClick(player.team.id) },
            modifier =
                Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth(),
        )
    }
}

/** Full-width filled button navigating to the player's team. */
@Composable
private fun ViewTeamButton(
    teamName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(onClick = onClick, modifier = modifier) {
        Text(text = stringResource(R.string.player_detail_view_team, teamName))
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
    }
}

/** All known label/value attributes of the player; missing values are skipped. */
@Composable
private fun PlayerAttributes(
    player: Player,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AttributeRow(
            label = stringResource(R.string.player_attribute_height),
            value = player.height,
        )
        AttributeRow(
            label = stringResource(R.string.player_attribute_weight),
            value = player.weight?.let { stringResource(R.string.player_attribute_weight_lbs, it) },
        )
        AttributeRow(
            label = stringResource(R.string.player_attribute_jersey_number),
            value = player.jerseyNumber,
        )
        AttributeRow(
            label = stringResource(R.string.player_attribute_college),
            value = player.college,
        )
        AttributeRow(
            label = stringResource(R.string.player_attribute_country),
            value = player.country,
        )
        AttributeRow(
            label = stringResource(R.string.player_attribute_draft),
            value =
                player.draftYear?.let { year ->
                    listOfNotNull(
                        "$year",
                        player.draftRound?.let {
                            stringResource(R.string.player_attribute_draft_round, it)
                        },
                        player.draftNumber?.let {
                            stringResource(R.string.player_attribute_draft_pick, it)
                        },
                    ).joinToString(", ")
                },
        )
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun PlayerDetailScreenPreview() {
    CourtFlowTheme(dynamicColor = false) {
        PlayerDetailScreen(
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
            onRetry = {},
            onShare = {},
            onNavigateBack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PlayerDetailScreenLoadingPreview() {
    CourtFlowTheme(dynamicColor = false) {
        PlayerDetailScreen(
            state = PlayerDetailState(isLoading = true),
            onTeamClick = {},
            onRetry = {},
            onShare = {},
            onNavigateBack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PlayerDetailScreenErrorPreview() {
    CourtFlowTheme(dynamicColor = false) {
        PlayerDetailScreen(
            state = PlayerDetailState(error = DataErrorKind.SERVER),
            onTeamClick = {},
            onRetry = {},
            onShare = {},
            onNavigateBack = {},
        )
    }
}
