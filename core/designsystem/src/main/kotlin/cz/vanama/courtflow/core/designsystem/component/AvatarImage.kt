package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import cz.vanama.courtflow.core.designsystem.animation.LocalNavAnimatedVisibilityScope
import cz.vanama.courtflow.core.designsystem.animation.LocalSharedTransitionScope
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

/**
 * Avatar image loaded by Glide on a tonal container, with a [loadingIcon]
 * placeholder while the request is in flight and a broken-image icon when
 * it fails. Circular by default; team emblems pass a rounded [shape] and
 * the secondary container colors.
 *
 * Centralizes the Glide configuration so every screen renders remote
 * artwork consistently.
 */
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AvatarImage(
    model: String,
    contentDescription: String?,
    loadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    sharedElementKey: Any? = null,
) {
    GlideImage(
        model = model,
        contentDescription = contentDescription,
        loading =
            placeholder {
                PlaceholderIcon(
                    icon = loadingIcon,
                    tint = contentColor,
                    modifier = Modifier.testTag(TestTags.AVATAR_LOADING),
                )
            },
        failure =
            placeholder {
                PlaceholderIcon(
                    icon = Icons.Filled.BrokenImage,
                    tint = contentColor,
                    modifier = Modifier.testTag(TestTags.AVATAR_FAILURE),
                )
            },
        modifier =
            modifier
                .sharedAvatarElement(sharedElementKey)
                .clip(shape)
                .background(containerColor),
    )
}

/**
 * Tags this avatar as a shared element keyed by [key] so it animates between screens. A no-op
 * unless [key] is non-null and both the [androidx.compose.animation.SharedTransitionScope] and the
 * navigation [androidx.compose.animation.AnimatedVisibilityScope] are present in the composition
 * (i.e. inside the app's shared-transition navigation host) — so previews, tests, and untagged
 * avatars are unaffected.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.sharedAvatarElement(key: Any?): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedVisibilityScope.current
    return if (key != null && sharedScope != null && animatedScope != null) {
        with(sharedScope) {
            sharedElement(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedScope,
            )
        }
    } else {
        this
    }
}

/** Placeholder icon centered within the avatar bounds. */
@Composable
private fun PlaceholderIcon(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
        )
    }
}

@PreviewLightDark
@Composable
private fun AvatarImagePreview() {
    CourtFlowTheme {
        // An empty model keeps the preview deterministic: Glide renders the
        // loading placeholder icon instead of fetching a remote image.
        AvatarImage(
            model = "",
            contentDescription = null,
            loadingIcon = Icons.Filled.Person,
            modifier = Modifier.size(64.dp),
        )
    }
}
