package cz.vanama.courtflow.core.designsystem.theme

import io.kotest.matchers.shouldBe
import org.junit.Test

class StaticColorSchemeTest {
    @Test
    fun `standard contrast returns the base light scheme`() {
        staticColorScheme(darkTheme = false, contrast = 0.0f, trueBlack = false).primary shouldBe PrimaryLight
    }

    @Test
    fun `standard contrast dark returns the base dark scheme`() {
        staticColorScheme(darkTheme = true, contrast = 0.0f, trueBlack = false).primary shouldBe PrimaryDark
    }

    @Test
    fun `medium contrast returns the medium light scheme`() {
        staticColorScheme(darkTheme = false, contrast = 0.5f, trueBlack = false).primary shouldBe
            PrimaryLightMediumContrast
    }

    @Test
    fun `high contrast returns the high dark scheme`() {
        staticColorScheme(darkTheme = true, contrast = 1.0f, trueBlack = false).primary shouldBe
            PrimaryDarkHighContrast
    }

    @Test
    fun `contrast wins over true-black`() {
        staticColorScheme(darkTheme = true, contrast = 1.0f, trueBlack = true).primary shouldBe
            PrimaryDarkHighContrast
    }

    @Test
    fun `true-black applies at standard contrast`() {
        staticColorScheme(darkTheme = true, contrast = 0.0f, trueBlack = true).surface shouldBe SurfaceAmoled
    }

    @Test
    fun `negative contrast clamps to standard`() {
        staticColorScheme(darkTheme = true, contrast = -1.0f, trueBlack = false).primary shouldBe PrimaryDark
    }
}
