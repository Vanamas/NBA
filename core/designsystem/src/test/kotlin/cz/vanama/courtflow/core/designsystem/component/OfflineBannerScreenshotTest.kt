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
 * Visual regression tests of [OfflineBanner].
 *
 * Golden images live in `src/test/screenshots`; re-record with
 * `./gradlew :core:designsystem:recordRoborazziDebug` and verify with
 * `./gradlew :core:designsystem:verifyRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class OfflineBannerScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun SampleOfflineBanner() {
        OfflineBanner(
            message = "Couldn’t refresh — showing cached data",
            onRetry = {},
        )
    }

    @Test
    fun offlineBannerLight() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = false) {
                SampleOfflineBanner()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun offlineBannerDark() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = true) {
                SampleOfflineBanner()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }
}
