package cz.vanama.courtflow.feature.teams.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cz.vanama.courtflow.core.designsystem.component.TestTags
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.model.Team
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TeamListContentTest {
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
    fun `shows loading indicator while loading`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                TeamListContent(
                    state = TeamListState(isLoading = true),
                    onTeamClick = {},
                    onRetry = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    @Test
    fun `shows teams and propagates clicks`() {
        var clickedId: Int? = null

        composeTestRule.setContent {
            CourtFlowTheme {
                TeamListContent(
                    state = TeamListState(sections = listOf(TeamSection("West", "Pacific", listOf(team)))),
                    onTeamClick = { clickedId = it },
                    onRetry = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Golden State Warriors").assertIsDisplayed()
        composeTestRule.onNodeWithText("West · Pacific").assertIsDisplayed()
        composeTestRule.onNodeWithText("Golden State Warriors").performClick()

        clickedId shouldBe 10
    }

    @Test
    fun `shows error text and retry button on error`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                TeamListContent(
                    state = TeamListState(error = DataErrorKind.SERVER),
                    onTeamClick = {},
                    onRetry = {},
                )
            }
        }

        composeTestRule.onNodeWithText("The server is having trouble. Try again later.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `retry button click invokes onRetry`() {
        var retries = 0

        composeTestRule.setContent {
            CourtFlowTheme {
                TeamListContent(
                    state = TeamListState(error = DataErrorKind.SERVER),
                    onTeamClick = {},
                    onRetry = { retries++ },
                )
            }
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        retries shouldBe 1
    }
}
