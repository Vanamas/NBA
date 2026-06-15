package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

private val CHIP_WIDTH = 96.dp
private val CHIP_AVATAR_SIZE = 56.dp

/**
 * Compact, fixed-width player card for the horizontal "recently viewed" strip:
 * a round portrait above the player name, tappable as a whole.
 *
 * @param imageUrl URL of the player portrait loaded by Glide; pass an empty
 *   string in previews/screenshot tests to keep rendering deterministic.
 */
@Composable
fun RecentPlayerChip(
    name: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(CHIP_WIDTH),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            AvatarImage(
                model = imageUrl,
                contentDescription = name,
                loadingIcon = Icons.Filled.Person,
                modifier = Modifier.size(CHIP_AVATAR_SIZE),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RecentPlayerChipPreview() {
    CourtFlowTheme {
        RecentPlayerChip(name = "Stephen Curry", imageUrl = "", onClick = {})
    }
}
