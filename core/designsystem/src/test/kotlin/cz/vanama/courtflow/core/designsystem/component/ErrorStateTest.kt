package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
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
class ErrorStateTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows message and retry button`() {
        composeTestRule.setContent {
            ErrorState(
                message = "Something went wrong",
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `clicking retry invokes onRetry`() {
        var retries = 0

        composeTestRule.setContent {
            ErrorState(
                message = "Something went wrong",
                onRetry = { retries++ },
            )
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        retries shouldBe 1
    }

    @Test
    fun `countdown variant shows the remaining seconds`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                ErrorState(
                    message = "Too many requests",
                    onRetry = {},
                    retryInSeconds = 12,
                )
            }
        }

        composeTestRule.onNodeWithText("Retrying in 12 s").assertIsDisplayed()
    }

    @Test
    fun `countdown variant disables the retry button`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                ErrorState(
                    message = "Too many requests",
                    onRetry = {},
                    retryInSeconds = 12,
                )
            }
        }

        composeTestRule.onNode(hasClickAction()).assertIsNotEnabled()
    }
}
