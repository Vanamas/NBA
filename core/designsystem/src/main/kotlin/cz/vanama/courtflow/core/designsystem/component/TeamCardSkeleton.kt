package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

private val NameBarWidth = 180.dp
private val NameBarHeight = 18.dp
private val SectionBarWidth = 140.dp
private val SectionBarHeight = 14.dp

/**
 * Grey placeholder mirroring [TeamCard]'s layout (two text bars), shown
 * while the team list is loading.
 *
 * @param shimmerEnabled animates the placeholder alpha when true; pass false
 * in screenshot tests to keep captures deterministic.
 */
@Composable
fun TeamCardSkeleton(
    modifier: Modifier = Modifier,
    shimmerEnabled: Boolean = true,
) {
    val alpha = shimmerAlpha(enabled = shimmerEnabled)
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(TestTags.TEAM_CARD_SKELETON),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .graphicsLayer { this.alpha = alpha.value },
        ) {
            SkeletonBox(width = NameBarWidth, height = NameBarHeight)
            SkeletonBox(width = SectionBarWidth, height = SectionBarHeight)
        }
    }
}

@PreviewLightDark
@Composable
private fun TeamCardSkeletonPreview() {
    CourtFlowTheme {
        TeamCardSkeleton(shimmerEnabled = false)
    }
}
