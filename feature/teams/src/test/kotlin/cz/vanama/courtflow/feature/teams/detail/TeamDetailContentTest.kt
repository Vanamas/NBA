package cz.vanama.courtflow.feature.teams.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import cz.vanama.courtflow.core.designsystem.component.TestTags
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.domain.model.Game
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

    private val game =
        Game(
            id = 1,
            date = "2026-06-10",
            homeTeam = team,
            homeTeamScore = 112,
            visitorTeam = lakers,
            visitorTeamScore = 99,
        )

    @Composable
    private fun pagingItems(
        players: List<Player> = emptyList(),
        loadStates: LoadStates? = null,
    ): LazyPagingItems<Player> {
        val pagingData =
            if (loadStates == null) {
                PagingData.from(players)
            } else {
                PagingData.from(players, sourceLoadStates = loadStates)
            }
        return flowOf(pagingData).collectAsLazyPagingItems()
    }

    private fun loadStates(
        refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
        append: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
    ): LoadStates =
        LoadStates(
            refresh = refresh,
            prepend = LoadState.NotLoading(endOfPaginationReached = false),
            append = append,
        )

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

        composeTestRule.onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
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
            .onNodeWithTag(TEAM_DETAIL_LIST_TEST_TAG)
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
            .onNodeWithTag(TEAM_DETAIL_LIST_TEST_TAG)
            .performScrollToNode(hasText("Stephen Curry"))
        composeTestRule.onNodeWithText("Stephen Curry").performClick()

        clickedId shouldBe 19
    }

    @Test
    fun `recent games section shows header, scores and date`() {
        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(team = team, recentGames = listOf(game)),
                players = pagingItems(roster),
                onRetry = {},
                onPlayerClick = {},
            )
        }

        composeTestRule
            .onNodeWithTag(TEAM_DETAIL_LIST_TEST_TAG)
            .performScrollToNode(hasText("Recent games"))
        composeTestRule.onNodeWithText("Recent games").assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(TEAM_DETAIL_LIST_TEST_TAG)
            .performScrollToNode(hasText("112 – 99"))
        composeTestRule.onNodeWithText("LAL").assertIsDisplayed()
        composeTestRule.onNodeWithText("112 – 99").assertIsDisplayed()
        composeTestRule.onNodeWithText("2026-06-10").assertIsDisplayed()
    }

    @Test
    fun `recent games section is hidden when there are no games`() {
        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(team = team, recentGames = emptyList()),
                players = pagingItems(roster),
                onRetry = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithText("Recent games").assertDoesNotExist()
    }

    @Test
    fun `roster refresh error shows error state with retry`() {
        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(team = team),
                players =
                    pagingItems(
                        loadStates = loadStates(refresh = LoadState.Error(DataException(DataErrorKind.NETWORK))),
                    ),
                onRetry = {},
                onPlayerClick = {},
            )
        }

        composeTestRule
            .onNodeWithTag(TEAM_DETAIL_LIST_TEST_TAG)
            .performScrollToNode(hasTestTag(ROSTER_REFRESH_ERROR_TEST_TAG))
        composeTestRule.onNodeWithTag(ROSTER_REFRESH_ERROR_TEST_TAG).assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Failed to load roster: No internet connection. Check your network and try again.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `roster refresh error with loaded players keeps roster and shows offline banner`() {
        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(team = team),
                players =
                    pagingItems(
                        players = roster,
                        loadStates = loadStates(refresh = LoadState.Error(DataException(DataErrorKind.NETWORK))),
                    ),
                onRetry = {},
                onPlayerClick = {},
            )
        }

        composeTestRule
            .onNodeWithTag(TEAM_DETAIL_LIST_TEST_TAG)
            .performScrollToNode(hasText("Stephen Curry"))
        composeTestRule.onNodeWithText("Stephen Curry").assertIsDisplayed()
        composeTestRule.onNodeWithTag(ROSTER_REFRESH_ERROR_TEST_TAG).assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEAM_DETAIL_LIST_TEST_TAG)
            .performScrollToNode(hasTestTag(TEAM_ROSTER_OFFLINE_BANNER_TEST_TAG))
        composeTestRule.onNodeWithTag(TEAM_ROSTER_OFFLINE_BANNER_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `roster append error shows inline error row with retry`() {
        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(team = team),
                players =
                    pagingItems(
                        players = roster,
                        loadStates = loadStates(append = LoadState.Error(DataException(DataErrorKind.NETWORK))),
                    ),
                onRetry = {},
                onPlayerClick = {},
            )
        }

        composeTestRule
            .onNodeWithTag(TEAM_DETAIL_LIST_TEST_TAG)
            .performScrollToNode(hasText("Error loading more players", substring = true))
        composeTestRule
            .onNodeWithText("Error loading more players: No internet connection. Check your network and try again.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `roster refresh with loaded players keeps content visible without roster spinner`() {
        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(team = team),
                players =
                    pagingItems(
                        players = roster,
                        loadStates = loadStates(refresh = LoadState.Loading),
                    ),
                onRetry = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithTag(TEAM_ROSTER_PULL_TO_REFRESH_TEST_TAG).assertExists()
        composeTestRule
            .onNodeWithTag(TEAM_DETAIL_LIST_TEST_TAG)
            .performScrollToNode(hasText("Stephen Curry"))
        composeTestRule.onNodeWithText("Stephen Curry").assertIsDisplayed()
        composeTestRule.onNodeWithTag(ROSTER_LOADING_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `roster refresh loading shows roster spinner`() {
        composeTestRule.setContent {
            TeamDetailContent(
                state = TeamDetailState(team = team),
                players = pagingItems(loadStates = loadStates(refresh = LoadState.Loading)),
                onRetry = {},
                onPlayerClick = {},
            )
        }

        composeTestRule
            .onNodeWithTag(TEAM_DETAIL_LIST_TEST_TAG)
            .performScrollToNode(hasTestTag(ROSTER_LOADING_TEST_TAG))
        composeTestRule.onNodeWithTag(ROSTER_LOADING_TEST_TAG).assertIsDisplayed()
    }
}
