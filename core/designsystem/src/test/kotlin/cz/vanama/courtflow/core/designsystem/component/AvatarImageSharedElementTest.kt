package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.animation.playerAvatarSharedKey
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Shared-element transitions need two screens and a frame clock and cannot be asserted under
 * Robolectric. These tests instead pin the *additive* contract: passing a [sharedElementKey]
 * with the shared-transition locals absent (their default `null`, as in previews/tests) must
 * render the avatar exactly as the untagged avatar does — no crash, placeholder still shown.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AvatarImageSharedElementTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setTaggedAvatar() {
        composeTestRule.setContent {
            CourtFlowTheme {
                AvatarImage(
                    model = "",
                    contentDescription = "Stephen Curry",
                    loadingIcon = Icons.Filled.Person,
                    modifier = Modifier.size(64.dp),
                    sharedElementKey = playerAvatarSharedKey(playerId = 19),
                )
            }
        }
    }

    @Test
    fun `tagged avatar renders without shared-transition scopes`() {
        setTaggedAvatar()

        // With no SharedTransitionScope in the composition the modifier is a no-op, so the
        // avatar resolves its empty model to the failure placeholder just like an untagged one.
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(TestTags.AVATAR_FAILURE), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun `key builder is stable and symmetric for a given id`() {
        check(playerAvatarSharedKey(19) == playerAvatarSharedKey(19)) {
            "key builder must be deterministic for the same id"
        }
        check(playerAvatarSharedKey(19) != playerAvatarSharedKey(20)) {
            "different players must get different shared-element keys"
        }
    }
}
