package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

private val AvatarSize = 64.dp
private val NameBarWidth = 160.dp
private val NameBarHeight = 20.dp
private val PositionBarWidth = 96.dp
private val PositionBarHeight = 14.dp
private val TeamBarWidth = 128.dp
private val TeamBarHeight = 12.dp
private val BarCornerRadius = 4.dp
private const val SHIMMER_MIN_ALPHA = 0.4f
private const val SHIMMER_MAX_ALPHA = 1f
private const val SHIMMER_DURATION_MILLIS = 1_000

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

/**
 * Constant full alpha when disabled, an infinite pulse otherwise. Returned
 * as [State] so callers read it inside the draw phase (graphicsLayer) and
 * the pulse animates without recomposing the skeleton every frame.
 */
@Composable
private fun shimmerAlpha(enabled: Boolean): State<Float> {
    if (!enabled) return remember { mutableStateOf(SHIMMER_MAX_ALPHA) }
    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
    return transition.animateFloat(
        initialValue = SHIMMER_MAX_ALPHA,
        targetValue = SHIMMER_MIN_ALPHA,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = SHIMMER_DURATION_MILLIS, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "skeleton_alpha",
    )
}

/** Single grey placeholder block. */
@Composable
private fun SkeletonBox(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(BarCornerRadius),
) {
    Box(
        modifier =
            modifier
                .size(width = width, height = height)
                .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = shape),
    )
}

@PreviewLightDark
@Composable
private fun PlayerCardSkeletonPreview() {
    CourtFlowTheme {
        PlayerCardSkeleton(shimmerEnabled = false)
    }
}
