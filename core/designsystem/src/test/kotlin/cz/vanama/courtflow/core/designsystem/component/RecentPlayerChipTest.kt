package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
class RecentPlayerChipTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows the player name and is clickable`() {
        var clicked = false
        composeTestRule.setContent {
            RecentPlayerChip(
                name = "Stephen Curry",
                imageUrl = "",
                onClick = { clicked = true },
            )
        }

        composeTestRule
            .onNodeWithText("Stephen Curry")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
        clicked shouldBe true
    }
}
