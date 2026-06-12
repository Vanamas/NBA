package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

/**
 * Single "label / value" row of a detail screen (Height, City, Draft, ...).
 * Renders nothing when [value] is `null` or blank, so optional attributes
 * can be passed straight through.
 */
@Composable
fun AttributeRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    labelWidth: Dp = 140.dp,
) {
    if (value.isNullOrBlank()) return
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(labelWidth).alignByBaseline(),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

@PreviewLightDark
@Composable
private fun AttributeRowPreview() {
    CourtFlowTheme {
        AttributeRow(label = "Height", value = "6-2")
    }
}
