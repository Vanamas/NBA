package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import cz.vanama.courtflow.core.designsystem.R
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ConnectivityBannerTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows the offline message`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                ConnectivityBanner()
            }
        }

        val expected = RuntimeEnvironment.getApplication().getString(R.string.connectivity_banner)
        composeTestRule.onNodeWithText(expected).assertIsDisplayed()
    }
}
