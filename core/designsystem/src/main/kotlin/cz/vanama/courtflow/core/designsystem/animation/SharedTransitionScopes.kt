package cz.vanama.courtflow.core.designsystem.animation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * The [SharedTransitionScope] of the single [androidx.compose.animation.SharedTransitionLayout]
 * that wraps the app's navigation host. `null` whenever a composable is rendered outside that
 * layout — previews, unit tests, and any screen reused without the shared-transition host — so
 * shared-element tagging is a no-op there.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope?> =
    compositionLocalOf { null }

/**
 * The [AnimatedVisibilityScope] of the currently composed navigation entry, used to drive the
 * shared element's enter/exit. `null` outside a navigation transition (previews, tests).
 */
val LocalNavAnimatedVisibilityScope: ProvidableCompositionLocal<AnimatedVisibilityScope?> =
    compositionLocalOf { null }

/** Stable shared-element key for a player's avatar, matched between the list card and the detail. */
fun playerAvatarSharedKey(playerId: Int): String = "player-avatar-$playerId"
