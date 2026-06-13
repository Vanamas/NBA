package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Visual regression tests of [Badge] covering both [BadgeTone]s.
 *
 * Golden images live in `src/test/screenshots`; re-record with
 * `./gradlew :core:designsystem:recordRoborazziDebug` and verify with
 * `./gradlew :core:designsystem:verifyRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class BadgeScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun SamplePositionBadge() {
        Badge(text = "G")
    }

    @Composable
    private fun SamplePrimaryBadge() {
        Badge(
            text = "GSW",
            tone = BadgeTone.Primary,
            textStyle = MaterialTheme.typography.labelMedium,
            minHeight = 26.dp,
        )
    }

    @Test
    fun badgePositionLight() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = false) {
                SamplePositionBadge()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun badgePositionDark() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = true) {
                SamplePositionBadge()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun badgePrimaryLight() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = false) {
                SamplePrimaryBadge()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun badgePrimaryDark() {
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = true) {
                SamplePrimaryBadge()
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }
}
