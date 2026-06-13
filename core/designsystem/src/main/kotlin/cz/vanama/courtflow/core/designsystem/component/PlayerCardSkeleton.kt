package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

private val AvatarSize = 64.dp
private val NameBarWidth = 160.dp
private val NameBarHeight = 20.dp
private val PositionBarWidth = 96.dp
private val PositionBarHeight = 14.dp
private val TeamBarWidth = 128.dp
private val TeamBarHeight = 12.dp

/**
 * Grey placeholder mirroring [PlayerCard]'s layout (64.dp avatar circle and
 * three text bars), shown while the first page of players is loading.
 *
 * @param shimmerEnabled animates the placeholder alpha when true; pass false
 * in screenshot tests to keep captures deterministic.
 */
@Composable
fun PlayerCardSkeleton(
    modifier: Modifier = Modifier,
    shimmerEnabled: Boolean = true,
) {
    val alpha = shimmerAlpha(enabled = shimmerEnabled)
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(TestTags.PLAYER_CARD_SKELETON),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .graphicsLayer { this.alpha = alpha.value },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBox(width = AvatarSize, height = AvatarSize, shape = CircleShape)
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(width = NameBarWidth, height = NameBarHeight)
                SkeletonBox(width = PositionBarWidth, height = PositionBarHeight)
                SkeletonBox(width = TeamBarWidth, height = TeamBarHeight)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PlayerCardSkeletonPreview() {
    CourtFlowTheme {
        PlayerCardSkeleton(shimmerEnabled = false)
    }
}
