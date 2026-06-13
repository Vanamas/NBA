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
 * Visual regression tests of [TeamCardSkeleton].
 *
 * Golden images live in `src/test/screenshots`; re-record with
 * `./gradlew :core:designsystem:recordRoborazziDebug` and verify with
 * `./gradlew :core:designsystem:verifyRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class TeamCardSkeletonScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun SampleTeamCardSkeleton() {
        // Shimmer off - the capture must be deterministic.
        TeamCardSkeleton(shimmerEnabled = false)
    }

    @Test
    fun teamCardSkeletonLight() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = false) {
                SampleTeamCardSkeleton()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun teamCardSkeletonDark() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = true) {
                SampleTeamCardSkeleton()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }
}
