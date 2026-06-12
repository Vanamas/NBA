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

    private fun primaryWith(dynamicColor: Boolean): Color {
        var primary = Color.Unspecified
        composeTestRule.setContent {
            CourtFlowTheme(darkTheme = false, dynamicColor = dynamicColor) {
                primary = MaterialTheme.colorScheme.primary
            }
        }
        composeTestRule.waitForIdle()
        return primary
    }

    @Test
    fun `static theme keeps the brand primary`() {
        primaryWith(dynamicColor = false) shouldBe PrimaryLight
    }

    @Test
    fun `dynamic theme swaps the palette on Android 12 plus`() {
        primaryWith(dynamicColor = true) shouldNotBe PrimaryLight
    }
}
