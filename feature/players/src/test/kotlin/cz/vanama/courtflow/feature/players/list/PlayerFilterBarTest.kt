package cz.vanama.courtflow.feature.players.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cz.vanama.courtflow.domain.model.Team
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlayerFilterBarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val lakers =
        Team(
            id = 14,
            abbreviation = "LAL",
            city = "Los Angeles",
            conference = "West",
            division = "Pacific",
            fullName = "Los Angeles Lakers",
            name = "Lakers",
        )

    @Test
    fun `position chips are displayed`() {
        composeTestRule.setContent {
            PlayerFilterBar(
                teams = listOf(lakers),
                selectedTeam = null,
                selectedPosition = null,
                onTeamSelected = {},
                onPositionSelected = {},
            )
        }

        composeTestRule.onNodeWithText("Guards").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forwards").assertIsDisplayed()
        composeTestRule.onNodeWithText("Centers").assertIsDisplayed()
    }

    @Test
    fun `tapping a position chip selects its code`() {
        var selected: String? = "untouched"

        composeTestRule.setContent {
            PlayerFilterBar(
                teams = listOf(lakers),
                selectedTeam = null,
                selectedPosition = null,
                onTeamSelected = {},
                onPositionSelected = { selected = it },
            )
        }

        composeTestRule.onNodeWithText("Guards").performClick()

        selected shouldBe "G"
    }

    @Test
    fun `re-tapping the selected position clears it`() {
        var selected: String? = "G"

        composeTestRule.setContent {
            PlayerFilterBar(
                teams = listOf(lakers),
                selectedTeam = null,
                selectedPosition = "G",
                onTeamSelected = {},
                onPositionSelected = { selected = it },
            )
        }

        composeTestRule.onNodeWithText("Guards").performClick()

        selected shouldBe null
    }

    @Test
    fun `team chip shows the selected team name`() {
        composeTestRule.setContent {
            PlayerFilterBar(
                teams = listOf(lakers),
                selectedTeam = lakers,
                selectedPosition = null,
                onTeamSelected = {},
                onPositionSelected = {},
            )
        }

        composeTestRule.onNodeWithText("Lakers").assertIsDisplayed()
    }
}
