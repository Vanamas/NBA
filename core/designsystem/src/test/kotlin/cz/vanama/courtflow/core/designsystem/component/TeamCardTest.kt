package cz.vanama.courtflow.core.designsystem.component

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
class TeamCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `card shows name conference and division`() {
        composeTestRule.setContent {
            TeamCard(
                fullName = "Golden State Warriors",
                conference = "West",
                division = "Pacific",
                abbreviation = "GSW",
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("Golden State Warriors").assertIsDisplayed()
        composeTestRule.onNodeWithText("West · Pacific").assertIsDisplayed()
    }

    @Test
    fun `clicking the card invokes onClick`() {
        var clicks = 0

        composeTestRule.setContent {
            TeamCard(
                fullName = "Golden State Warriors",
                conference = "West",
                division = "Pacific",
                abbreviation = "GSW",
                onClick = { clicks++ },
            )
        }

        composeTestRule.onNodeWithText("Golden State Warriors").performClick()

        clicks shouldBe 1
    }

    @Test
    fun `card shows the abbreviation badge`() {
        composeTestRule.setContent {
            TeamCard(
                fullName = "Golden State Warriors",
                conference = "West",
                division = "Pacific",
                abbreviation = "GSW",
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("GSW").assertIsDisplayed()
    }

    @Test
    fun `blank abbreviation renders no badge`() {
        composeTestRule.setContent {
            TeamCard(
                fullName = "Golden State Warriors",
                conference = "West",
                division = "Pacific",
                abbreviation = "",
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("Golden State Warriors").assertIsDisplayed()
    }
}
