package cz.vanama.courtflow.feature.teams.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cz.vanama.courtflow.core.designsystem.component.ErrorState
import cz.vanama.courtflow.core.designsystem.component.PagingAppendError
import cz.vanama.courtflow.core.designsystem.component.PagingAppendLoading
import cz.vanama.courtflow.core.designsystem.component.PlayerCard
import cz.vanama.courtflow.core.designsystem.component.errorMessage
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.teams.R

internal const val ROSTER_REFRESH_ERROR_TEST_TAG = "roster_refresh_error"
internal const val ROSTER_LOADING_TEST_TAG = "roster_loading"

/** Roster rows: section header, player cards and the trailing append state. */
internal fun LazyListScope.rosterItems(
    team: Team,
    players: LazyPagingItems<Player>,
    onPlayerClick: (Int) -> Unit,
) {
    if (players.itemCount > 0) {
        item { SectionHeader(text = stringResource(R.string.team_detail_roster)) }
    }

    items(
        count = players.itemCount,
        key = players.itemKey { it.id },
        contentType = players.itemContentType(),
    ) { index ->
        val player = players[index]
        if (player != null) {
            PlayerCard(
                firstName = player.firstName,
                lastName = player.lastName,
                position = player.position,
                teamName = team.fullName,
                imageUrl = player.imageUrl,
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
        is LoadState.Loading -> item { PagingAppendLoading() }
        is LoadState.Error ->
            item {
                PagingAppendError(
                    message = stringResource(R.string.team_roster_append_error, errorMessage(appendState.error)),
                    onRetry = { players.retry() },
                )
            }
        else -> {}
    }
}

/** Section title above a list block (roster, recent games). */
@Composable
internal fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
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
    PagingAppendLoading(modifier = modifier.testTag(ROSTER_LOADING_TEST_TAG))
}
