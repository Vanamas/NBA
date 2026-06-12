package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val SHIMMER_MIN_ALPHA = 0.4f
private const val SHIMMER_MAX_ALPHA = 1f
private const val SHIMMER_DURATION_MILLIS = 1_000
private val BarCornerRadius = 4.dp

/**
 * Constant full alpha when disabled, an infinite pulse otherwise. Returned
 * as [State] so callers read it inside the draw phase (graphicsLayer) and
 * the pulse animates without recomposing the skeleton every frame.
 *
 * @param enabled pass false in screenshot tests to keep captures deterministic.
 */
@Composable
internal fun shimmerAlpha(enabled: Boolean): State<Float> {
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

/** Single grey placeholder block shared by the loading skeletons. */
@Composable
internal fun SkeletonBox(
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
