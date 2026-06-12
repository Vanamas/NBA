package cz.vanama.courtflow.feature.teams.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
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

    private val roster =
        listOf(
            Player(id = 19, firstName = "Stephen", lastName = "Curry", position = "G", team = team),
        )

    @Composable
    private fun pagingItems(players: List<Player> = emptyList()): LazyPagingItems<Player> =
        flowOf(PagingData.from(players)).collectAsLazyPagingItems()

    @Test
    fun `loading state shows progress indicator`() {
        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(isLoading = true),
                players = pagingItems(),
                onRetry = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun `error state shows error message and retry button`() {
        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(error = DataErrorKind.NETWORK),
                players = pagingItems(),
                onRetry = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithText("No internet connection. Check your network and try again.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `retry button click invokes onRetry`() {
        var retries = 0

        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(error = DataErrorKind.NETWORK),
                players = pagingItems(),
                onRetry = { retries++ },
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        retries shouldBe 1
    }

    @Test
    fun `team state shows all team information`() {
        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(team = team),
                players = pagingItems(),
                onRetry = {},
                onPlayerClick = {},
            )
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

    @Test
    fun `roster section renders the team players`() {
        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(team = team),
                players = pagingItems(roster),
                onRetry = {},
                onPlayerClick = {},
            )
        }

        composeTestRule
            .onNodeWithTag("team_detail_list")
            .performScrollToNode(hasText("Stephen Curry"))
        composeTestRule.onNodeWithText("Roster").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stephen Curry").assertIsDisplayed()
    }

    @Test
    fun `tapping a roster row invokes onPlayerClick with the player id`() {
        var clickedId: Int? = null

        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(team = team),
                players = pagingItems(roster),
                onRetry = {},
                onPlayerClick = { clickedId = it },
            )
        }

        composeTestRule
            .onNodeWithTag("team_detail_list")
            .performScrollToNode(hasText("Stephen Curry"))
        composeTestRule.onNodeWithText("Stephen Curry").performClick()

        clickedId shouldBe 19
    }
}
