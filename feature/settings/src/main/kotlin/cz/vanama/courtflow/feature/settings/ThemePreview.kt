package cz.vanama.courtflow.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A live swatch of the active color scheme shown at the top of the settings
 * screen, so theme choices (dynamic color, true-black) are visible in place
 * instead of only after navigating away. Recomposes automatically as the
 * persisted preferences update the surrounding theme.
 */
@Composable
internal fun ThemePreviewCard() {
    val description = stringResource(R.string.settings_preview_description)
    SectionHeader(stringResource(R.string.settings_preview_section))
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Swatch(MaterialTheme.colorScheme.primary)
        Swatch(MaterialTheme.colorScheme.secondary)
        Swatch(MaterialTheme.colorScheme.tertiary)
        Swatch(MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(
        modifier =
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color),
    )
}
