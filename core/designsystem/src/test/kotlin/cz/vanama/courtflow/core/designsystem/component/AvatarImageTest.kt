package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AvatarImageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setAvatar(model: String = "") {
        composeTestRule.setContent {
            CourtFlowTheme {
                AvatarImage(
                    model = model,
                    contentDescription = "Stephen Curry",
                    loadingIcon = Icons.Filled.Person,
                    modifier = Modifier.size(64.dp),
                )
            }
        }
    }

    /** Glide delivers the failure for an empty model asynchronously. */
    private fun awaitFailurePlaceholder() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(TestTags.AVATAR_FAILURE), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun `empty model resolves to the failure placeholder`() {
        setAvatar(model = "")

        awaitFailurePlaceholder()
    }

    @Test
    fun `failure placeholder replaces the loading placeholder`() {
        setAvatar(model = "")

        awaitFailurePlaceholder()

        composeTestRule
            .onAllNodes(hasTestTag(TestTags.AVATAR_LOADING), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .let { nodes -> check(nodes.isEmpty()) { "loading placeholder still present after failure" } }
    }
}
