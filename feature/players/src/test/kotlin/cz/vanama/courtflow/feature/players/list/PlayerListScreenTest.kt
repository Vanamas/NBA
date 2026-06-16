package cz.vanama.courtflow.feature.players.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.feature.players.R
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlayerListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun string(resId: Int) = RuntimeEnvironment.getApplication().getString(resId)

    @Test
    fun `filter action shows an active badge when a filter is selected`() {
        setScreen(selectedPosition = "G")

        composeTestRule.onNodeWithContentDescription(string(R.string.player_filter_open)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FILTER_ACTIVE_BADGE_TEST_TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `filter action has no badge when no filter is selected`() {
        setScreen()

        composeTestRule.onNodeWithContentDescription(string(R.string.player_filter_open)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FILTER_ACTIVE_BADGE_TEST_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    private fun setScreen(selectedPosition: String? = null) {
        composeTestRule.setContent {
            CourtFlowTheme {
                PlayerListScreen(
                    players = flowOf(PagingData.empty<Player>()).collectAsLazyPagingItems(),
                    searchQuery = "",
                    isOffline = false,
                    onSearchQueryChanged = {},
                    onPlayerClick = {},
                    selectedPosition = selectedPosition,
                )
            }
        }
    }
}
