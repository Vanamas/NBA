package cz.vanama.courtflow.feature.settings

import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import cz.vanama.courtflow.core.common.settings.ThemeMode
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `dynamic color switch reflects state and emits intent`() {
        var emitted: SettingsIntent? = null
        rule.setContent {
            SettingsContent(
                state = SettingsState(dynamicColor = true),
                onIntent = { emitted = it },
                currentLanguageTag = "",
                onLanguageSelected = {},
            )
        }
        rule.onNodeWithText("Use wallpaper colors").assertIsOn().performClick()
        (emitted as SettingsIntent.OnDynamicColorChanged).enabled shouldBe false
    }

    @Test
    fun `selecting Dark emits the theme-mode intent`() {
        var emitted: SettingsIntent? = null
        rule.setContent {
            SettingsContent(
                state = SettingsState(),
                onIntent = { emitted = it },
                currentLanguageTag = "",
                onLanguageSelected = {},
            )
        }
        rule.onNodeWithText("Dark").performClick()
        (emitted as SettingsIntent.OnThemeModeChanged).mode shouldBe ThemeMode.DARK
    }

    @Test
    fun `selecting Czech emits its language tag`() {
        var picked: String? = "unset"
        rule.setContent {
            SettingsContent(
                state = SettingsState(),
                onIntent = {},
                currentLanguageTag = "",
                onLanguageSelected = { picked = it },
            )
        }
        rule.onNodeWithText("Čeština").performScrollTo().performClick()
        picked shouldBe "cs"
    }
}
