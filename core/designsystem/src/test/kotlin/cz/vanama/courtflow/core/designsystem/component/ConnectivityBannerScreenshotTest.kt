package cz.vanama.courtflow.core.designsystem.component

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
 * Visual regression tests of [ConnectivityBanner].
 *
 * Golden images live in `src/test/screenshots`; re-record with
 * `./gradlew :core:designsystem:recordRoborazziDebug` and verify with
 * `./gradlew :core:designsystem:verifyRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class ConnectivityBannerScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun connectivityBannerLight() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = false) {
                ConnectivityBanner()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun connectivityBannerDark() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = true) {
                ConnectivityBanner()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }
}
