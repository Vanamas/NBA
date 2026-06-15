package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FavoriteCardIndicatorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `player card shows the favorite indicator when favorited`() {
        composeTestRule.setContent {
            PlayerCard(
                firstName = "Stephen",
                lastName = "Curry",
                position = "G",
                teamName = "Golden State Warriors",
                imageUrl = "",
                isFavorite = true,
                onClick = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Favorite").assertIsDisplayed()
    }

    @Test
    fun `player card hides the favorite indicator by default`() {
        composeTestRule.setContent {
            PlayerCard(
                firstName = "Stephen",
                lastName = "Curry",
                position = "G",
                teamName = "Golden State Warriors",
                imageUrl = "",
                onClick = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Favorite").assertDoesNotExist()
    }

    @Test
    fun `team card shows the favorite indicator when favorited`() {
        composeTestRule.setContent {
            TeamCard(
                fullName = "Golden State Warriors",
                conference = "West",
                division = "Pacific",
                abbreviation = "GSW",
                isFavorite = true,
                onClick = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Favorite").assertIsDisplayed()
    }
}
