package cz.vanama.courtflow.feature.players.list

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.feature.players.R
import cz.vanama.courtflow.core.designsystem.R as DesignR

internal const val OFFLINE_BANNER_TEST_TAG = "player_offline_banner"

/**
 * Compact inline banner pinned above the player list when a refresh failed
 * but cached items are still available: the user keeps the list and can
 * [onRetry] the refresh without losing it.
 */
@Composable
internal fun OfflineBanner(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(OFFLINE_BANNER_TEST_TAG),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.player_list_offline_banner),
                style = MaterialTheme.typography.bodyMedium,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(DesignR.string.retry))
            }
        }
    }
}
