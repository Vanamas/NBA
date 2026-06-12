package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder

/**
 * Avatar image loaded by Glide on a tonal container, with a [loadingIcon]
 * placeholder while the request is in flight and a broken-image icon when
 * it fails. Circular by default; team emblems pass a rounded [shape] and
 * the secondary container colors.
 *
 * Centralizes the Glide configuration so every screen renders remote
 * artwork consistently.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AvatarImage(
    model: String,
    contentDescription: String?,
    loadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                .clip(shape)
                .background(containerColor),
    )
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
