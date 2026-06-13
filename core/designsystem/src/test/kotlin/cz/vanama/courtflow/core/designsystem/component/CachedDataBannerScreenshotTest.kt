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
 * Visual regression tests of [CachedDataBanner].
 *
 * Golden images live in `src/test/screenshots`; re-record with
 * `./gradlew :core:designsystem:recordRoborazziDebug` and verify with
 * `./gradlew :core:designsystem:verifyRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class CachedDataBannerScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun SampleCachedDataBanner() {
        CachedDataBanner(
            message = "Couldn’t refresh — showing cached data",
            onRetry = {},
        )
    }

    @Test
    fun cachedDataBannerLight() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = false) {
                SampleCachedDataBanner()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun cachedDataBannerDark() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = true) {
                SampleCachedDataBanner()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }
}
