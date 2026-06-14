package cz.vanama.courtflow.ui

import androidx.compose.ui.test.junit4.createComposeRule
import cz.vanama.courtflow.core.common.settings.ThemeMode
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CourtFlowAppTest {
    @get:Rule
    val rule = createComposeRule()

    @After
    fun tearDown() {
        // CourtFlowApplication (instantiated by Robolectric) starts Koin; stop it.
        stopKoin()
    }

    private fun darkFor(mode: ThemeMode): Boolean {
        var dark = false
        rule.setContent { dark = resolveDarkTheme(mode) }
        rule.waitForIdle()
        return dark
    }

    @Test
    fun `light mode is never dark`() {
        assertFalse(darkFor(ThemeMode.LIGHT))
    }

    @Test
    fun `dark mode is always dark`() {
        assertTrue(darkFor(ThemeMode.DARK))
    }

    @Test
    fun `system mode follows a light device`() {
        assertFalse(darkFor(ThemeMode.SYSTEM))
    }

    @Test
    @Config(sdk = [35], qualifiers = "night")
    fun `system mode follows a dark device`() {
        assertTrue(darkFor(ThemeMode.SYSTEM))
    }
}
