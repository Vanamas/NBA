package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
 * Visual regression tests of [ErrorState].
 *
 * Golden images live in `src/test/screenshots`; re-record with
 * `./gradlew :core:designsystem:recordRoborazziDebug` and verify with
 * `./gradlew :core:designsystem:verifyRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class ErrorStateScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun SampleErrorState() {
        ErrorState(
            message = "Failed to load players: HTTP 500",
            onRetry = {},
        )
    }

    @Test
    fun errorStateLight() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = false) {
                SampleErrorState()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun errorStateDark() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = true) {
                SampleErrorState()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun errorStateCountdownLight() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = false) {
                ErrorState(
                    message = "Too many requests. Please wait a moment and try again.",
                    onRetry = {},
                    retryInSeconds = 12,
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }
}
