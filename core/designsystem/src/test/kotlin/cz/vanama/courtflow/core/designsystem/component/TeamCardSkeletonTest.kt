package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TeamCardSkeletonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `skeleton with shimmer is displayed`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                TeamCardSkeleton()
            }
        }

        composeTestRule.onNodeWithTag(TestTags.TEAM_CARD_SKELETON).assertIsDisplayed()
    }

    @Test
    fun `skeleton with shimmer disabled is displayed`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                TeamCardSkeleton(shimmerEnabled = false)
            }
        }

        composeTestRule.onNodeWithTag(TestTags.TEAM_CARD_SKELETON).assertIsDisplayed()
    }
}
