package cz.vanama.courtflow.feature.teams.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.designsystem.component.TestTags
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
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
    fun `renders section headers in order with fallback last`() {
        val celtics = Team(1, "BOS", "Boston", "East", "Atlantic", "Boston Celtics", "Celtics")
        val bullets = Team(2, "BAL", "Baltimore", "", "", "Baltimore Bullets", "Bullets")

        composeTestRule.setContent {
            CourtFlowTheme {
                TeamListContent(
                    state =
                        TeamListState(
                            sections =
                                listOf(
                                    TeamSection("East", "Atlantic", listOf(celtics)),
                                    TeamSection("West", "Pacific", listOf(team)),
                                    TeamSection("", "", listOf(bullets)),
                                ),
                        ),
                    onTeamClick = {},
                    onRetry = {},
                )
            }
        }

        composeTestRule.onNodeWithText("East — Atlantic").assertIsDisplayed()
        composeTestRule.onNodeWithText("West — Pacific").assertIsDisplayed()
        composeTestRule.onNodeWithText("Other teams").assertIsDisplayed()

        val eastTop =
            composeTestRule
                .onNodeWithText("East — Atlantic")
                .fetchSemanticsNode()
                .boundsInRoot.top
        val westTop =
            composeTestRule
                .onNodeWithText("West — Pacific")
                .fetchSemanticsNode()
                .boundsInRoot.top
        val otherTop =
            composeTestRule
                .onNodeWithText("Other teams")
                .fetchSemanticsNode()
                .boundsInRoot.top

        (eastTop < westTop) shouldBe true
        (westTop < otherTop) shouldBe true
    }

    @Test
    fun `section with blank division shows conference-only header`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                TeamListContent(
                    state = TeamListState(sections = listOf(TeamSection("East", "", listOf(team)))),
                    onTeamClick = {},
                    onRetry = {},
                )
            }
        }

        composeTestRule.onNodeWithText("East").assertIsDisplayed()
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
