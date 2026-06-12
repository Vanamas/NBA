package cz.vanama.courtflow.feature.players.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
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
class PlayerListContentTest {
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
            team = team,
        )

    @Test
    fun `initial refresh shows loading indicator`() {
        val loadingStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            )

        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.empty<Player>(sourceLoadStates = loadingStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun `players are displayed with name position and team`() {
        composeTestRule.setContent {
            PlayerListContent(
                players = flowOf(PagingData.from(listOf(player))).collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithText("Stephen Curry").assertIsDisplayed()
        composeTestRule.onNodeWithText("G").assertIsDisplayed()
        composeTestRule.onNodeWithText("Golden State Warriors").assertIsDisplayed()
    }

    @Test
    fun `clicking a player invokes callback with player id`() {
        var clickedPlayerId: Int? = null

        composeTestRule.setContent {
            PlayerListContent(
                players = flowOf(PagingData.from(listOf(player))).collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = { clickedPlayerId = it },
            )
        }

        composeTestRule.onNodeWithText("Stephen Curry").performClick()

        clickedPlayerId shouldBe 19
    }

    @Test
    fun `typing into search field invokes callback`() {
        var lastQuery = ""

        composeTestRule.setContent {
            PlayerListContent(
                players = flowOf(PagingData.empty<Player>()).collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = { lastQuery = it },
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithTag("player_search_field").performTextInput("curry")

        lastQuery shouldBe "curry"
    }

    @Test
    fun `refresh error shows message and retry button`() {
        val errorStates =
            LoadStates(
                refresh = LoadState.Error(RuntimeException("boom")),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            )

        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.empty<Player>(sourceLoadStates = errorStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithTag("refresh_error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }
}
