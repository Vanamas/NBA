package cz.vanama.courtflow.feature.settings

import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
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

    private val languages = listOf("en", "cs")

    private fun setContent(
        state: SettingsState = SettingsState(languageTags = languages, versionName = "1.0"),
        onIntent: (SettingsIntent) -> Unit = {},
    ) {
        rule.setContent { SettingsContent(state = state, onIntent = onIntent) }
    }

    @Test
    fun `renders the theme preview header`() {
        setContent()
        rule.onNodeWithText("Preview").assertExists()
    }

    @Test
    fun `dynamic color switch reflects state and emits intent`() {
        var emitted: SettingsIntent? = null
        setContent(state = SettingsState(dynamicColor = true, languageTags = languages), onIntent = { emitted = it })
        rule.onNodeWithText("Use wallpaper colors").assertIsOn().performClick()
        (emitted as SettingsIntent.OnDynamicColorChanged).enabled shouldBe false
    }

    @Test
    fun `true-black switch emits its intent`() {
        var emitted: SettingsIntent? = null
        setContent(onIntent = { emitted = it })
        rule.onNodeWithText("Pure black dark theme").performScrollTo().performClick()
        (emitted as SettingsIntent.OnTrueBlackChanged).enabled shouldBe true
    }

    @Test
    fun `selecting Light emits the theme-mode intent`() {
        var emitted: SettingsIntent? = null
        setContent(onIntent = { emitted = it })
        rule.onNodeWithText("Light").performScrollTo().performClick()
        (emitted as SettingsIntent.OnThemeModeChanged).mode shouldBe ThemeMode.LIGHT
    }

    @Test
    fun `selecting Dark emits the theme-mode intent`() {
        var emitted: SettingsIntent? = null
        setContent(onIntent = { emitted = it })
        rule.onNodeWithText("Dark").performScrollTo().performClick()
        (emitted as SettingsIntent.OnThemeModeChanged).mode shouldBe ThemeMode.DARK
    }

    @Test
    fun `selecting English emits its language tag`() {
        var picked: String? = "unset"
        setContent(onIntent = { if (it is SettingsIntent.OnLanguageSelected) picked = it.tag })
        rule.onNodeWithText("English").performScrollTo().performClick()
        picked shouldBe "en"
    }

    @Test
    fun `selecting Czech emits its language tag`() {
        var picked: String? = "unset"
        setContent(onIntent = { if (it is SettingsIntent.OnLanguageSelected) picked = it.tag })
        rule.onNodeWithText("Čeština").performScrollTo().performClick()
        picked shouldBe "cs"
    }

    @Test
    fun `renders the About header`() {
        setContent()
        rule.onNodeWithText("About").performScrollTo().assertExists()
    }

    @Test
    fun `shows the app version name`() {
        setContent(state = SettingsState(languageTags = languages, versionName = "1.0"))
        rule.onNodeWithText("1.0").performScrollTo().assertExists()
    }

    @Test
    fun `tapping open-source licenses emits its intent`() {
        var emitted: SettingsIntent? = null
        setContent(onIntent = { emitted = it })
        rule.onNodeWithText("Open-source licenses").performScrollTo().performClick()
        (emitted shouldBe SettingsIntent.OnOssLicensesClicked)
    }

    @Test
    fun `selecting the follow-system language emits an empty tag`() {
        var picked: String? = "unset"
        setContent(
            state = SettingsState(currentLanguageTag = "cs", languageTags = languages),
            onIntent = { if (it is SettingsIntent.OnLanguageSelected) picked = it.tag },
        )
        // Both the theme and language sections have a "Follow system" row; the
        // language one is composed last.
        rule
            .onAllNodesWithText("Follow system")
            .onLast()
            .performScrollTo()
            .performClick()
        picked shouldBe ""
    }
}
