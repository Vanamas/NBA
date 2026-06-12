package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.R
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

/**
 * Centered error message with a retry button, used as the full-screen
 * failure state of a loading operation.
 *
 * @param message human-readable description of the failure.
 * @param onRetry called when the user taps the retry button.
 * @param retryInSeconds when non-null, the retry happens automatically in
 *   this many seconds: the button is disabled and shows the countdown.
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryInSeconds: Int? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(16.dp),
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRetry,
            enabled = retryInSeconds == null,
        ) {
            Text(
                text =
                    if (retryInSeconds == null) {
                        stringResource(R.string.retry)
                    } else {
                        stringResource(R.string.retrying_in, retryInSeconds)
                    },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ErrorStatePreview() {
    CourtFlowTheme {
        ErrorState(
            message = "Failed to load players: HTTP 500",
            onRetry = {},
        )
    }
}
