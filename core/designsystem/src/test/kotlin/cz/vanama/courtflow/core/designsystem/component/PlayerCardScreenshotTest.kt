package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Visual regression tests of [PlayerCard].
 *
 * Golden images live in `src/test/screenshots`; re-record with
 * `./gradlew :core:designsystem:recordRoborazziDebug` and verify with
 * `./gradlew :core:designsystem:verifyRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PlayerCardScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun SamplePlayerCard() {
        PlayerCard(
            firstName = "Stephen",
            lastName = "Curry",
            position = "G",
            teamName = "Golden State Warriors",
            // Deliberately not a real URL - the image slot stays empty, keeping the screenshot deterministic.
            imageUrl = "",
            onClick = {},
        )
    }

    @Test
    fun playerCardLight() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = false, dynamicColor = false) {
                SamplePlayerCard()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun playerCardDark() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = true, dynamicColor = false) {
                SamplePlayerCard()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }
}
