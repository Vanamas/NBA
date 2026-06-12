package cz.vanama.courtflow.feature.players.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlayerDetailContentTest {
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

    private val player =
        Player(
            id = 19,
            firstName = "Stephen",
            lastName = "Curry",
            position = "G",
            height = "6-2",
            weight = "185",
            jerseyNumber = "30",
            college = "Davidson",
            country = "USA",
            draftYear = 2009,
            draftRound = 1,
            draftNumber = 7,
            team = team,
        )

    @Test
    fun `loading state shows progress indicator`() {
        composeTestRule.setContent {
            PlayerDetailContent(
                state = PlayerDetailState(isLoading = true),
                onTeamClick = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun `error state shows error message and retry button`() {
        composeTestRule.setContent {
            PlayerDetailContent(
                state = PlayerDetailState(error = "Network error"),
                onTeamClick = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `retry button click invokes onRetry`() {
        var retries = 0

        composeTestRule.setContent {
            PlayerDetailContent(
                state = PlayerDetailState(error = "Network error"),
                onTeamClick = {},
                onRetry = { retries++ },
            )
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        retries shouldBe 1
    }

    @Test
    fun `player state shows all known attributes`() {
        composeTestRule.setContent {
            PlayerDetailContent(
                state = PlayerDetailState(player = player),
                onTeamClick = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithText("Stephen Curry").assertIsDisplayed()
        composeTestRule.onNodeWithText("G").assertIsDisplayed()
        composeTestRule.onNodeWithText("Guard").assertIsDisplayed()
        composeTestRule.onNodeWithText("6-2").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("185 lbs").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Davidson").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("USA").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("2009, round 1, pick 7").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `missing optional attributes are not shown`() {
        val rookie =
            player.copy(
                height = null,
                weight = null,
                jerseyNumber = null,
                college = null,
                country = null,
                draftYear = null,
                draftRound = null,
                draftNumber = null,
            )

        composeTestRule.setContent {
            PlayerDetailContent(
                state = PlayerDetailState(player = rookie),
                onTeamClick = {},
                onRetry = {},
            )
        }

        composeTestRule.onNodeWithText("Height").assertDoesNotExist()
        composeTestRule.onNodeWithText("College").assertDoesNotExist()
        composeTestRule.onNodeWithText("Draft").assertDoesNotExist()
    }

    @Test
    fun `team button click invokes callback with team id`() {
        var clickedTeamId: Int? = null

        composeTestRule.setContent {
            PlayerDetailContent(
                state = PlayerDetailState(player = player),
                onTeamClick = { clickedTeamId = it },
                onRetry = {},
            )
        }

        composeTestRule
            .onNodeWithText("View Team: Golden State Warriors")
            .performScrollTo()
            .performClick()

        clickedTeamId shouldBe 10
    }
}
