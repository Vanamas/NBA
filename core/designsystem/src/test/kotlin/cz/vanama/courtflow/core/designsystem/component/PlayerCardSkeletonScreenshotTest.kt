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
 * Visual regression tests of [PlayerCardSkeleton].
 *
 * Golden images live in `src/test/screenshots`; re-record with
 * `./gradlew :core:designsystem:recordRoborazziDebug` and verify with
 * `./gradlew :core:designsystem:verifyRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PlayerCardSkeletonScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun SamplePlayerCardSkeleton() {
        // Shimmer off - the capture must be deterministic.
        PlayerCardSkeleton(shimmerEnabled = false)
    }

    @Test
    fun playerCardSkeletonLight() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = false) {
                SamplePlayerCardSkeleton()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun playerCardSkeletonDark() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = true) {
                SamplePlayerCardSkeleton()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }
}
