package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.R
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

/** Full-width banner telling the user the device is offline. */
@Composable
fun ConnectivityBanner(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.connectivity_banner),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun ConnectivityBannerPreview() {
    CourtFlowTheme {
        ConnectivityBanner()
    }
}
