package cz.vanama.courtflow.feature.teams.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import cz.vanama.courtflow.core.designsystem.component.ErrorState
import cz.vanama.courtflow.core.designsystem.component.PlayerCard
import cz.vanama.courtflow.core.designsystem.util.PlaceholderImages
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.teams.R
import cz.vanama.courtflow.feature.teams.errorMessage
import cz.vanama.courtflow.core.designsystem.R as DesignR

internal const val ROSTER_REFRESH_ERROR_TEST_TAG = "roster_refresh_error"
internal const val ROSTER_LOADING_TEST_TAG = "roster_loading"

/** Roster rows: section header, player cards and the trailing append state. */
internal fun LazyListScope.rosterItems(
    team: Team,
    players: LazyPagingItems<Player>,
    onPlayerClick: (Int) -> Unit,
) {
    if (players.itemCount > 0) {
        item { RosterHeader() }
    }

    items(count = players.itemCount) { index ->
        val player = players[index]
        if (player != null) {
            PlayerCard(
                firstName = player.firstName,
                lastName = player.lastName,
                position = player.position,
                teamName = team.fullName,
                imageUrl = PlaceholderImages.playerPortrait(player.id, size = 128),
                onClick = { onPlayerClick(player.id) },
                modifier =
                    Modifier
                        .widthIn(max = 360.dp)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 8.dp),
            )
        }
    }

    when (val appendState = players.loadState.append) {
        is LoadState.Loading -> item { RosterAppendLoading() }
        is LoadState.Error ->
            item {
                RosterAppendError(
                    error = appendState,
                    onRetry = { players.retry() },
                )
            }
        else -> {}
    }
}

/** Section title above the roster list. */
@Composable
private fun RosterHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.team_detail_roster),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier =
            modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 8.dp),
    )
}

/** Full-width roster failure state for the first roster page. */
@Composable
internal fun RosterRefreshError(
    error: LoadState.Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ErrorState(
        message = stringResource(R.string.team_roster_refresh_error, errorMessage(error.error)),
        onRetry = onRetry,
        modifier = modifier.testTag(ROSTER_REFRESH_ERROR_TEST_TAG),
    )
}

/** Spinner shown while the first roster page is loading. */
@Composable
internal fun RosterLoading(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .testTag(ROSTER_LOADING_TEST_TAG),
    )
}

/** Inline next-page failure row with a retry button. */
@Composable
private fun RosterAppendError(
    error: LoadState.Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.team_roster_append_error, errorMessage(error.error)),
            modifier = Modifier.padding(16.dp),
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(DesignR.string.retry))
        }
    }
}

/** Inline next-page loading row. */
@Composable
private fun RosterAppendLoading(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentWidth(Alignment.CenterHorizontally),
    )
}
