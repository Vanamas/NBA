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
class PlayerCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `card shows name position and team`() {
        composeTestRule.setContent {
            PlayerCard(
                firstName = "Stephen",
                lastName = "Curry",
                position = "G",
                teamName = "Golden State Warriors",
                imageUrl = "https://example.com/portrait.jpg",
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("Stephen Curry").assertIsDisplayed()
        composeTestRule.onNodeWithText("G").assertIsDisplayed()
        composeTestRule.onNodeWithText("Guard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Golden State Warriors").assertIsDisplayed()
    }

    @Test
    fun `clicking the card invokes onClick`() {
        var clicks = 0

        composeTestRule.setContent {
            PlayerCard(
                firstName = "Stephen",
                lastName = "Curry",
                position = "G",
                teamName = "Golden State Warriors",
                imageUrl = "https://example.com/portrait.jpg",
                onClick = { clicks++ },
            )
        }

        composeTestRule.onNodeWithText("Stephen Curry").performClick()

        clicks shouldBe 1
    }
}
