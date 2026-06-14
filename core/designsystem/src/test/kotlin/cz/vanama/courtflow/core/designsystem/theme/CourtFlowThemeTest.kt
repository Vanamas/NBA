package cz.vanama.courtflow.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CourtFlowThemeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun primaryWith(
        dynamicColor: Boolean,
        darkTheme: Boolean = false,
    ): Color {
        var primary = Color.Unspecified
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                primary = MaterialTheme.colorScheme.primary
            }
        }
        composeTestRule.waitForIdle()
        return primary
    }

    private fun surfaceWith(
        darkTheme: Boolean,
        trueBlack: Boolean,
    ): Color {
        var surface = Color.Unspecified
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = darkTheme, dynamicColor = false, trueBlack = trueBlack) {
                surface = MaterialTheme.colorScheme.surface
            }
        }
        composeTestRule.waitForIdle()
        return surface
    }

    @Test
    fun `static theme keeps the brand primary`() {
        primaryWith(dynamicColor = false) shouldBe PrimaryLight
    }

    @Test
    fun `static dark theme keeps the brand dark primary`() {
        primaryWith(dynamicColor = false, darkTheme = true) shouldBe PrimaryDark
    }

    @Test
    fun `dynamic theme swaps the palette on Android 12 plus`() {
        primaryWith(dynamicColor = true) shouldNotBe PrimaryLight
    }

    @Test
    fun `dynamic dark theme swaps the palette on Android 12 plus`() {
        primaryWith(dynamicColor = true, darkTheme = true) shouldNotBe PrimaryDark
    }

    @Test
    fun `true-black dark theme uses pure black surface`() {
        surfaceWith(darkTheme = true, trueBlack = true) shouldBe Color.Black
    }

    @Test
    fun `true-black flag is ignored in light theme`() {
        surfaceWith(darkTheme = false, trueBlack = true) shouldBe SurfaceLight
    }
}
