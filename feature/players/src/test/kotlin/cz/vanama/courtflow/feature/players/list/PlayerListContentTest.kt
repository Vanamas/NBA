package cz.vanama.courtflow.feature.players.list

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.text.input.ImeAction
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import cz.vanama.courtflow.core.designsystem.component.TestTags
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
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
    fun `initial refresh shows skeleton placeholders instead of spinner`() {
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
                isOffline = false,
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        composeTestRule
            .onAllNodesWithTag(TestTags.PLAYER_CARD_SKELETON)
            .onFirst()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertDoesNotExist()
    }

    @Test
    fun `refresh with loaded items keeps list visible without centered spinner`() {
        val refreshingStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            )

        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.from(listOf(player), sourceLoadStates = refreshingStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
                isOffline = false,
            )
        }

        composeTestRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).assertExists()
        composeTestRule.onNodeWithText("Stephen Curry").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertDoesNotExist()
    }

    @Test
    fun `players are displayed with name position and team`() {
        composeTestRule.setContent {
            PlayerListContent(
                players = flowOf(PagingData.from(listOf(player))).collectAsLazyPagingItems(),
                searchQuery = "",
                isOffline = false,
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
                isOffline = false,
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
                isOffline = false,
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
                isOffline = false,
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithTag(REFRESH_ERROR_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `refresh error with cached items keeps list visible and shows offline banner`() {
        val errorStates =
            LoadStates(
                refresh = LoadState.Error(RuntimeException("offline")),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            )

        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.from(listOf(player), sourceLoadStates = errorStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
                isOffline = false,
            )
        }

        composeTestRule.onNodeWithText("Stephen Curry").assertIsDisplayed()
        composeTestRule.onNodeWithTag(REFRESH_ERROR_TEST_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(OFFLINE_BANNER_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Couldn’t refresh — showing cached data").assertIsDisplayed()
    }

    @Test
    fun `retry button on offline banner is enabled and clickable`() {
        val errorStates =
            LoadStates(
                refresh = LoadState.Error(RuntimeException("offline")),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            )

        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.from(listOf(player), sourceLoadStates = errorStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
                isOffline = false,
            )
        }

        // Same rationale as the append-error retry test: a static PagingData has a
        // no-op UiReceiver behind retry(), so the honest assertion is that the
        // banner offers an enabled, clickable button and clicking does not crash.
        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()
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
                isOffline = false,
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
                isOffline = false,
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
                isOffline = false,
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
                isOffline = false,
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
                isOffline = false,
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
                isOffline = false,
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
                isOffline = false,
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
                isOffline = false,
            )
        }

        composeTestRule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Clear search").performClick()

        lastQuery shouldBe ""
    }

    @Test
    fun `pull down gesture triggers a paging refresh`() {
        var pagingSourcesCreated = 0
        val pager =
            Pager(PagingConfig(pageSize = 35)) {
                pagingSourcesCreated++
                FakePlayerPagingSource(listOf(player))
            }

        composeTestRule.setContent {
            PlayerListContent(
                players = pager.flow.collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
                isOffline = false,
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) { pagingSourcesCreated == 1 }
        composeTestRule.onNodeWithText("Stephen Curry").assertIsDisplayed()

        composeTestRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }

        // refresh() invalidates the current PagingSource; Paging asks the factory for a new one.
        composeTestRule.waitUntil(timeoutMillis = 5_000) { pagingSourcesCreated == 2 }
    }

    @Test
    fun `search field uses the Search ime action`() {
        composeTestRule.setContent {
            PlayerListContent(
                players = flowOf(PagingData.from(listOf(player))).collectAsLazyPagingItems(),
                searchQuery = "",
                onSearchQueryChanged = {},
                onPlayerClick = {},
                isOffline = false,
            )
        }

        composeTestRule.onNodeWithTag(SEARCH_FIELD_TEST_TAG).assert(hasImeAction(ImeAction.Search))
    }

    @Test
    fun `connectivity banner is shown when offline`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                PlayerListContent(
                    players = flowOf(PagingData.from(emptyList<Player>())).collectAsLazyPagingItems(),
                    searchQuery = "",
                    onSearchQueryChanged = {},
                    onPlayerClick = {},
                    isOffline = true,
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.CONNECTIVITY_BANNER).assertIsDisplayed()
    }

    @Test
    fun `refresh error shows the rate-limit countdown when retryInSeconds is set`() {
        val errorStates =
            LoadStates(
                refresh = LoadState.Error(RuntimeException("rate limited")),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            )

        composeTestRule.setContent {
            PlayerListContent(
                players =
                    flowOf(PagingData.empty<Player>(sourceLoadStates = errorStates))
                        .collectAsLazyPagingItems(),
                searchQuery = "",
                isOffline = false,
                retryInSeconds = 7,
                onSearchQueryChanged = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithText("Retrying in 7 s").assertIsDisplayed()
    }
}

/** Single static page; each refresh makes the Pager factory build a new instance. */
private class FakePlayerPagingSource(
    private val players: List<Player>,
) : PagingSource<Int, Player>() {
    override fun getRefreshKey(state: PagingState<Int, Player>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Player> =
        LoadResult.Page(data = players, prevKey = null, nextKey = null)
}
