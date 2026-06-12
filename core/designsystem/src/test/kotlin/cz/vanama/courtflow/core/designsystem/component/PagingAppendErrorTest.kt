package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PagingAppendErrorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows the message and a retry button`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                PagingAppendError(message = "Loading failed", onRetry = {})
            }
        }

        composeTestRule.onNodeWithText("Loading failed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `clicking retry invokes onRetry`() {
        var retries = 0

        composeTestRule.setContent {
            CourtFlowTheme {
                PagingAppendError(message = "Loading failed", onRetry = { retries++ })
            }
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        retries shouldBe 1
    }
}
