package cz.vanama.courtflow.feature.players.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.players.R
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlayerFilterSheetTest {
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

    private fun string(resId: Int) = RuntimeEnvironment.getApplication().getString(resId)

    @Test
    fun `position chips are displayed`() {
        setSheet()

        composeTestRule.onNodeWithText(string(R.string.player_filter_position_guard)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.player_filter_position_forward)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.player_filter_position_center)).assertIsDisplayed()
    }

    @Test
    fun `tapping a position chip selects its code`() {
        var selected: String? = "untouched"
        setSheet(onPositionSelected = { selected = it })

        composeTestRule.onNodeWithText(string(R.string.player_filter_position_guard)).performClick()

        selected shouldBe "G"
    }

    @Test
    fun `re-tapping the selected position clears it`() {
        var selected: String? = "G"
        setSheet(selectedPosition = "G", onPositionSelected = { selected = it })

        composeTestRule.onNodeWithText(string(R.string.player_filter_position_guard)).performClick()

        selected shouldBe null
    }

    @Test
    fun `team chips include the all-teams entry and each team abbreviation`() {
        setSheet(teams = listOf(lakers))

        composeTestRule.onNodeWithText(string(R.string.player_filter_all_teams)).assertIsDisplayed()
        composeTestRule.onNodeWithText("LAL").assertIsDisplayed()
    }

    @Test
    fun `tapping a team chip selects that team`() {
        var selected: Team? = null
        setSheet(teams = listOf(lakers), onTeamSelected = { selected = it })

        composeTestRule.onNodeWithText("LAL").performClick()

        selected shouldBe lakers
    }

    @Test
    fun `clear button resets both filters`() {
        var team: Team? = lakers
        var position: String? = "G"
        setSheet(
            teams = listOf(lakers),
            selectedTeam = lakers,
            selectedPosition = "G",
            onTeamSelected = { team = it },
            onPositionSelected = { position = it },
        )

        composeTestRule.onNodeWithText(string(R.string.player_filter_clear)).performClick()

        team shouldBe null
        position shouldBe null
    }

    private fun setSheet(
        teams: List<Team> = emptyList(),
        selectedTeam: Team? = null,
        selectedPosition: String? = null,
        onTeamSelected: (Team?) -> Unit = {},
        onPositionSelected: (String?) -> Unit = {},
    ) {
        composeTestRule.setContent {
            PlayerFilterSheetContent(
                teams = teams,
                selectedTeam = selectedTeam,
                selectedPosition = selectedPosition,
                onTeamSelected = onTeamSelected,
                onPositionSelected = onPositionSelected,
            )
        }
    }
}
