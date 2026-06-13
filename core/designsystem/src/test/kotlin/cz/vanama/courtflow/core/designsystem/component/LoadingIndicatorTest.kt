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
class LoadingIndicatorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders with the shared loading indicator tag`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                LoadingIndicator()
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
    }
}
