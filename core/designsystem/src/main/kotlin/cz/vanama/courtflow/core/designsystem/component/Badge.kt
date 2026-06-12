package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.core.designsystem.theme.CourtOrange
import cz.vanama.courtflow.core.designsystem.theme.OnCourtOrange

/** Color tone of a [Badge]. */
enum class BadgeTone {
    /** Brand orange position badge (G / F / C) used across player surfaces. */
    Position,

    /** Material primary container, e.g. the team abbreviation. */
    Primary,
}

/**
 * Small status pill. Defaults to the brand-orange "position" badge
 * (G / F / C); [tone] switches to the Material container palette.
 *
 * @param text short uppercase content, e.g. `G` or `GSW`.
 * @param textStyle override for larger badges (defaults to labelSmall).
 * @param minHeight pill height; the design uses 22dp for position codes
 * and 26dp for the team abbreviation.
 */
@Composable
fun Badge(
    text: String,
    modifier: Modifier = Modifier,
    tone: BadgeTone = BadgeTone.Position,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    minHeight: Dp = 22.dp,
) {
    val (background, content) = badgeColors(tone)
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .defaultMinSize(minWidth = minHeight, minHeight = minHeight)
                .background(color = background, shape = CircleShape)
                .padding(horizontal = 7.dp),
    ) {
        Text(
            text = text,
            style = textStyle,
            fontWeight = FontWeight.Bold,
            color = content,
            maxLines = 1,
        )
    }
}

@Composable
private fun badgeColors(tone: BadgeTone): Pair<Color, Color> =
    when (tone) {
        BadgeTone.Position -> CourtOrange to OnCourtOrange
        BadgeTone.Primary ->
            MaterialTheme.colorScheme.primaryContainer to
                MaterialTheme.colorScheme.onPrimaryContainer
    }

@Preview(showBackground = true)
@Composable
private fun BadgePreview() {
    CourtFlowTheme(dynamicColor = false) {
        Badge(text = "G")
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgePrimaryPreview() {
    CourtFlowTheme(dynamicColor = false) {
        Badge(
            text = "GSW",
            tone = BadgeTone.Primary,
            textStyle = MaterialTheme.typography.labelMedium,
            minHeight = 26.dp,
        )
    }
}
