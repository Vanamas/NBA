package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
}
