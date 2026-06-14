package cz.vanama.courtflow.feature.settings

import app.cash.turbine.test
import cz.vanama.courtflow.core.common.settings.ThemeMode
import cz.vanama.courtflow.core.common.settings.ThemePreferences
import cz.vanama.courtflow.core.common.settings.ThemePreferencesStore
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

class SettingsViewModelTest {
    private val prefsFlow = MutableStateFlow(ThemePreferences())
    private val store: ThemePreferencesStore = mockk(relaxed = true)
    private val localeController: AppLocaleController =
        mockk(relaxed = true) {
            every { currentLanguageTag() } returns ""
            every { supportedLanguageTags() } returns listOf("en", "cs")
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { store.themePreferences } returns prefsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SettingsViewModel(store, localeController)

    @Test
    fun `mirrors store preferences into state`() =
        runTest {
            val viewModel = viewModel()
            viewModel.uiState.test {
                awaitItem() shouldBe SettingsState(currentLanguageTag = "", languageTags = listOf("en", "cs"))
                prefsFlow.value =
                    ThemePreferences(dynamicColor = false, themeMode = ThemeMode.DARK, trueBlack = true)
                awaitItem() shouldBe
                    SettingsState(
                        dynamicColor = false,
                        themeMode = ThemeMode.DARK,
                        trueBlack = true,
                        currentLanguageTag = "",
                        languageTags = listOf("en", "cs"),
                    )
            }
        }

    @Test
    fun `seeds the current language and supported tags from the controller`() =
        runTest {
            every { localeController.currentLanguageTag() } returns "cs"
            viewModel().uiState.value.let {
                it.currentLanguageTag shouldBe "cs"
                it.languageTags shouldBe listOf("en", "cs")
            }
        }

    @Test
    fun `dynamic color intent persists through the store`() =
        runTest {
            viewModel().onIntent(SettingsIntent.OnDynamicColorChanged(false))
            coVerify { store.setDynamicColor(false) }
        }

    @Test
    fun `theme mode intent persists through the store`() =
        runTest {
            viewModel().onIntent(SettingsIntent.OnThemeModeChanged(ThemeMode.LIGHT))
            coVerify { store.setThemeMode(ThemeMode.LIGHT) }
        }

    @Test
    fun `true-black intent persists through the store`() =
        runTest {
            viewModel().onIntent(SettingsIntent.OnTrueBlackChanged(true))
            coVerify { store.setTrueBlack(true) }
        }

    @Test
    fun `language intent applies the locale and updates state`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onIntent(SettingsIntent.OnLanguageSelected("cs"))
            verify { localeController.setLanguage("cs") }
            viewModel.uiState.value.currentLanguageTag shouldBe "cs"
        }

    @Test
    fun `a write failure surfaces a PreferenceWriteFailed effect`() =
        runTest {
            coEvery { store.setDynamicColor(any()) } throws IOException("disk full")
            val viewModel = viewModel()
            viewModel.uiEffect.test {
                viewModel.onIntent(SettingsIntent.OnDynamicColorChanged(false))
                awaitItem() shouldBe SettingsEffect.PreferenceWriteFailed
            }
        }
}
