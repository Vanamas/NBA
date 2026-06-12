package cz.vanama.courtflow.feature.teams.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import cz.vanama.courtflow.domain.model.Team
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TeamDetailContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val team =
        Team(
            id = 10,
            abbreviation = "GSW",
            city = "Golden State",
            conference = "West",
            division = "Pacific",
            fullName = "Golden State Warriors",
            name = "Warriors",
        )

    @Test
    fun `loading state shows progress indicator`() {
        composeTestRule.setContent {
            TeamDetailContent(state = TeamDetailState(isLoading = true))
        }

        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun `error state shows error message`() {
        composeTestRule.setContent {
            TeamDetailContent(state = TeamDetailState(error = "Network error"))
        }

        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }

    @Test
    fun `team state shows all team information`() {
        composeTestRule.setContent {
            TeamDetailContent(state = TeamDetailState(team = team))
        }

        composeTestRule.onNodeWithText("Golden State Warriors").assertIsDisplayed()
        composeTestRule.onNodeWithText("GSW").assertIsDisplayed()
        composeTestRule.onNodeWithText("City").assertIsDisplayed()
        composeTestRule.onNodeWithText("Golden State").assertIsDisplayed()
        composeTestRule.onNodeWithText("Conference").assertIsDisplayed()
        composeTestRule.onNodeWithText("West").assertIsDisplayed()
        composeTestRule.onNodeWithText("Division").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pacific").assertIsDisplayed()
    }
}
