package cz.vanama.courtflow.feature.players.list

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.ImeAction
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import cz.vanama.courtflow.core.designsystem.component.TestTags
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

    private val emptyNotLoadingStates =
        LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
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

        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
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

        composeTestRule.onNodeWithTag(SEARCH_FIELD_TEST_TAG).performTextInput("curry")

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

        composeTestRule.onNodeWithTag(REFRESH_ERROR_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `append loading shows spinner row below items`() {
        val appendLoadingStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.Loading,
            )

        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.from(listOf(player), sourceLoadStates = appendLoadingStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        // The append spinner row carries no test tag. With refresh = NotLoading the
        // centered refresh spinner (TestTags.LOADING_INDICATOR) is not composed, so
        // the append row is the only indeterminate progress indicator on screen.
        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertDoesNotExist()
        composeTestRule.onNodeWithText("Stephen Curry").assertIsDisplayed()
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `append error shows message and retry button`() {
        val appendErrorStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.Error(RuntimeException("boom")),
            )

        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.from(listOf(player), sourceLoadStates = appendErrorStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        // player_list_append_error = "Error loading more players: %1$s"; a plain
        // RuntimeException is not a DataException, so errorMessage() resolves to
        // error_unknown = "Something went wrong. Please try again.". The append row
        // sits at the bottom of the LazyColumn, hence performScrollTo() first.
        composeTestRule
            .onNodeWithText("Error loading more players: Something went wrong. Please try again.")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Retry")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `retry button on append error row is enabled and clickable`() {
        val appendErrorStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.Error(RuntimeException("boom")),
            )

        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.from(listOf(player), sourceLoadStates = appendErrorStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        // Clicking dispatches LazyPagingItems.retry() (PlayerListScreen.kt wires
        // AppendError(onRetry = { players.retry() })). On a static PagingData.from
        // there is no real Pager behind the items, so retry() lands in a no-op
        // paging UiReceiver — no JVM-observable side effect exists to assert on.
        // What is honestly assertable here: the row offers an enabled button with
        // a click action, and clicking it does not crash. The onRetry wiring itself
        // is a one-line pass-through, verified by inspection.
        composeTestRule
            .onNodeWithText("Retry")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()
    }

    @Test
    fun `empty search results show message and clear search button`() {
        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.from(emptyList<Player>(), sourceLoadStates = emptyNotLoadingStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "xyzzy",
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithTag(EMPTY_STATE_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("No players found for “xyzzy”").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear search").assertIsDisplayed()
    }

    @Test
    fun `clear search button resets the query`() {
        var lastQuery: String? = null

        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.from(emptyList<Player>(), sourceLoadStates = emptyNotLoadingStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "xyzzy",
                onSearchQueryChanged = { lastQuery = it },
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithText("Clear search").performClick()

        lastQuery shouldBe ""
    }

    @Test
    fun `empty catalog without search shows generic message without clear button`() {
        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.from(emptyList<Player>(), sourceLoadStates = emptyNotLoadingStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithText("No players available").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear search").assertDoesNotExist()
    }

    @Test
    fun `clear icon is hidden when query is empty`() {
        composeTestRule.setContent {
            PlayerListContent(
                players = flowOf(PagingData.from(listOf(player))).collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Clear search").assertDoesNotExist()
    }

    @Test
    fun `clear icon with non-empty query resets the query`() {
        var lastQuery: String? = null

        composeTestRule.setContent {
            PlayerListContent(
                players = flowOf(PagingData.from(listOf(player))).collectAsLazyPagingItems(),
                searchQuery = "curry",
                onSearchQueryChanged = { lastQuery = it },
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Clear search").performClick()

        lastQuery shouldBe ""
    }

    @Test
    fun `search field uses the Search ime action`() {
        composeTestRule.setContent {
            PlayerListContent(
                players = flowOf(PagingData.from(listOf(player))).collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithTag(SEARCH_FIELD_TEST_TAG).assert(hasImeAction(ImeAction.Search))
    }
}
