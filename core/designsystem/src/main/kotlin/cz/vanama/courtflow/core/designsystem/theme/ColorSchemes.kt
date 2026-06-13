package cz.vanama.courtflow.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

internal val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryDark,
        onPrimary = OnPrimaryDark,
        primaryContainer = PrimaryContainerDark,
        onPrimaryContainer = OnPrimaryContainerDark,
        secondary = SecondaryDark,
        onSecondary = OnSecondaryDark,
        secondaryContainer = SecondaryContainerDark,
        onSecondaryContainer = OnSecondaryContainerDark,
        tertiary = TertiaryDark,
        onTertiary = OnTertiaryDark,
        tertiaryContainer = TertiaryContainerDark,
        onTertiaryContainer = OnTertiaryContainerDark,
        error = ErrorDark,
        onError = OnErrorDark,
        errorContainer = ErrorContainerDark,
        onErrorContainer = OnErrorContainerDark,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
    )

internal val LightColorScheme =
    lightColorScheme(
        primary = PrimaryLight,
        onPrimary = OnPrimaryLight,
        primaryContainer = PrimaryContainerLight,
        onPrimaryContainer = OnPrimaryContainerLight,
        secondary = SecondaryLight,
        onSecondary = OnSecondaryLight,
        secondaryContainer = SecondaryContainerLight,
        onSecondaryContainer = OnSecondaryContainerLight,
        tertiary = TertiaryLight,
        onTertiary = OnTertiaryLight,
        tertiaryContainer = TertiaryContainerLight,
        onTertiaryContainer = OnTertiaryContainerLight,
        error = ErrorLight,
        onError = OnErrorLight,
        errorContainer = ErrorContainerLight,
        onErrorContainer = OnErrorContainerLight,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
    )

/** Pure-black dark variant: only background/surface differ from [DarkColorScheme]. */
internal val AmoledDarkColorScheme =
    DarkColorScheme.copy(
        background = BackgroundAmoled,
        surface = SurfaceAmoled,
    )

internal val LightColorSchemeMediumContrast =
    LightColorScheme.copy(
        primary = PrimaryLightMediumContrast,
        onPrimary = OnPrimaryLightMediumContrast,
        background = BackgroundLightMediumContrast,
        onBackground = OnBackgroundLightMediumContrast,
        surface = SurfaceLightMediumContrast,
        onSurface = OnSurfaceLightMediumContrast,
    )

internal val LightColorSchemeHighContrast =
    LightColorScheme.copy(
        primary = PrimaryLightHighContrast,
        onPrimary = OnPrimaryLightHighContrast,
        background = BackgroundLightHighContrast,
        onBackground = OnBackgroundLightHighContrast,
        surface = SurfaceLightHighContrast,
        onSurface = OnSurfaceLightHighContrast,
    )

internal val DarkColorSchemeMediumContrast =
    DarkColorScheme.copy(
        primary = PrimaryDarkMediumContrast,
        onPrimary = OnPrimaryDarkMediumContrast,
        background = BackgroundDarkMediumContrast,
        onBackground = OnBackgroundDarkMediumContrast,
        surface = SurfaceDarkMediumContrast,
        onSurface = OnSurfaceDarkMediumContrast,
    )

internal val DarkColorSchemeHighContrast =
    DarkColorScheme.copy(
        primary = PrimaryDarkHighContrast,
        onPrimary = OnPrimaryDarkHighContrast,
        background = BackgroundDarkHighContrast,
        onBackground = OnBackgroundDarkHighContrast,
        surface = SurfaceDarkHighContrast,
        onSurface = OnSurfaceDarkHighContrast,
    )

/**
 * Selects the brand [ColorScheme] for [darkTheme]. Accessibility wins over
 * aesthetics: the medium/high-contrast variants apply per the system
 * [contrast] in `[-1, 1]` (from `UiModeManager.getContrast()`; values `<= 0`
 * mean standard), and only when contrast is standard does [trueBlack] swap the
 * dark scheme for the pure-black AMOLED variant.
 */
internal fun staticColorScheme(
    darkTheme: Boolean,
    contrast: Float,
    trueBlack: Boolean,
): ColorScheme =
    when {
        contrast >= HIGH_CONTRAST_THRESHOLD ->
            if (darkTheme) DarkColorSchemeHighContrast else LightColorSchemeHighContrast
        contrast >= MEDIUM_CONTRAST_THRESHOLD ->
            if (darkTheme) DarkColorSchemeMediumContrast else LightColorSchemeMediumContrast
        darkTheme && trueBlack -> AmoledDarkColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

private const val MEDIUM_CONTRAST_THRESHOLD = 1f / 3f
private const val HIGH_CONTRAST_THRESHOLD = 2f / 3f
