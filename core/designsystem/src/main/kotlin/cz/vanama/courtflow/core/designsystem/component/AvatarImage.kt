package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder

/**
 * Circle-cropped image loaded by Glide with a [loadingIcon] placeholder
 * while the request is in flight and a broken-image icon when it fails.
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
) {
    GlideImage(
        model = model,
        contentDescription = contentDescription,
        loading =
            placeholder {
                Icon(
                    imageVector = loadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        failure =
            placeholder {
                Icon(
                    imageVector = Icons.Filled.BrokenImage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        modifier = modifier.clip(CircleShape),
    )
}
