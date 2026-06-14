package cz.vanama.courtflow.feature.settings

import app.cash.turbine.test
import cz.vanama.courtflow.core.common.settings.ThemeMode
import cz.vanama.courtflow.core.common.settings.ThemePreferences
import cz.vanama.courtflow.core.common.settings.ThemePreferencesRepository
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {
    private val prefsFlow = MutableStateFlow(ThemePreferences())
    private val repository: ThemePreferencesRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { repository.themePreferences } returns prefsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `mirrors repository preferences into state`() =
        runTest {
            val viewModel = SettingsViewModel(repository)
            viewModel.uiState.test {
                awaitItem() shouldBe SettingsState()
                prefsFlow.value =
                    ThemePreferences(dynamicColor = false, themeMode = ThemeMode.DARK, trueBlack = true)
                awaitItem() shouldBe
                    SettingsState(dynamicColor = false, themeMode = ThemeMode.DARK, trueBlack = true)
            }
        }

    @Test
    fun `dynamic color intent persists through the repository`() =
        runTest {
            val viewModel = SettingsViewModel(repository)
            viewModel.onIntent(SettingsIntent.OnDynamicColorChanged(false))
            coVerify { repository.setDynamicColor(false) }
        }

    @Test
    fun `theme mode intent persists through the repository`() =
        runTest {
            val viewModel = SettingsViewModel(repository)
            viewModel.onIntent(SettingsIntent.OnThemeModeChanged(ThemeMode.LIGHT))
            coVerify { repository.setThemeMode(ThemeMode.LIGHT) }
        }

    @Test
    fun `true-black intent persists through the repository`() =
        runTest {
            val viewModel = SettingsViewModel(repository)
            viewModel.onIntent(SettingsIntent.OnTrueBlackChanged(true))
            coVerify { repository.setTrueBlack(true) }
        }
}
