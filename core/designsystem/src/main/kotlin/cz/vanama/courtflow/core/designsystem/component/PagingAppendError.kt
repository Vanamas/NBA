package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.R
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

/**
 * Inline next-page failure row with a retry button, appended below a
 * paginated list when loading the next page failed.
 *
 * @param message localized description of the failure, formatted by the caller.
 * @param onRetry called when the user taps the retry button.
 */
@Composable
fun PagingAppendError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@PreviewLightDark
@Composable
private fun PagingAppendErrorPreview() {
    CourtFlowTheme {
        PagingAppendError(
            message = "Couldn’t load more players",
            onRetry = {},
        )
    }
}
