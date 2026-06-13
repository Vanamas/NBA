package cz.vanama.courtflow.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Brand accent taken from the basketball launcher mark; used for the
 * position badge. Identical in light and dark theme, always paired
 * with [OnCourtOrange] text for contrast.
 */
val CourtOrange = Color(0xFFFF9800)
val OnCourtOrange = Color(0xFF1A1C1E)

val PrimaryLight = Color(0xFF0061A4)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFD1E4FF)
val OnPrimaryContainerLight = Color(0xFF001D36)

val SecondaryLight = Color(0xFF535F70)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFD7E3F7)
val OnSecondaryContainerLight = Color(0xFF101C2B)

val TertiaryLight = Color(0xFF6B5778)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFF2DAFF)
val OnTertiaryContainerLight = Color(0xFF251431)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFFDFCFF)
val OnBackgroundLight = Color(0xFF1A1C1E)
val SurfaceLight = Color(0xFFFDFCFF)
val OnSurfaceLight = Color(0xFF1A1C1E)

val PrimaryDark = Color(0xFF9ECAFF)
val OnPrimaryDark = Color(0xFF003258)
val PrimaryContainerDark = Color(0xFF00497D)
val OnPrimaryContainerDark = Color(0xFFD1E4FF)

val SecondaryDark = Color(0xFFBBC7DB)
val OnSecondaryDark = Color(0xFF253140)
val SecondaryContainerDark = Color(0xFF3B4858)
val OnSecondaryContainerDark = Color(0xFFD7E3F7)

val TertiaryDark = Color(0xFFD7BEE4)
val OnTertiaryDark = Color(0xFF3B2948)
val TertiaryContainerDark = Color(0xFF523F5F)
val OnTertiaryContainerDark = Color(0xFFF2DAFF)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF1A1C1E)
val OnBackgroundDark = Color(0xFFE2E2E6)
val SurfaceDark = Color(0xFF1A1C1E)
val OnSurfaceDark = Color(0xFFE2E2E6)

/**
 * Pure-black surfaces for the optional AMOLED dark theme; only the
 * background/surface roles differ from [BackgroundDark]/[SurfaceDark].
 */
val BackgroundAmoled = Color(0xFF000000)
val SurfaceAmoled = Color(0xFF000000)

/*
 * Medium- and high-contrast brand variants used when the Android 14+ system
 * "Increase contrast" setting is on. Directionally hand-tuned from the base
 * brand palette (deeper primaries + pushed on-colors); replace with a Material
 * Theme Builder export (seed #0061A4) for pixel-accurate Material tones.
 */
val PrimaryLightMediumContrast = Color(0xFF004B80)
val OnPrimaryLightMediumContrast = Color(0xFFFFFFFF)
val BackgroundLightMediumContrast = Color(0xFFFDFCFF)
val OnBackgroundLightMediumContrast = Color(0xFF0F1115)
val SurfaceLightMediumContrast = Color(0xFFFDFCFF)
val OnSurfaceLightMediumContrast = Color(0xFF0F1115)

val PrimaryLightHighContrast = Color(0xFF00294A)
val OnPrimaryLightHighContrast = Color(0xFFFFFFFF)
val BackgroundLightHighContrast = Color(0xFFFDFCFF)
val OnBackgroundLightHighContrast = Color(0xFF000000)
val SurfaceLightHighContrast = Color(0xFFFDFCFF)
val OnSurfaceLightHighContrast = Color(0xFF000000)

val PrimaryDarkMediumContrast = Color(0xFFC3DDFF)
val OnPrimaryDarkMediumContrast = Color(0xFF00264A)
val BackgroundDarkMediumContrast = Color(0xFF1A1C1E)
val OnBackgroundDarkMediumContrast = Color(0xFFFAFAFD)
val SurfaceDarkMediumContrast = Color(0xFF1A1C1E)
val OnSurfaceDarkMediumContrast = Color(0xFFFAFAFD)

val PrimaryDarkHighContrast = Color(0xFFEBF1FF)
val OnPrimaryDarkHighContrast = Color(0xFF000000)
val BackgroundDarkHighContrast = Color(0xFF1A1C1E)
val OnBackgroundDarkHighContrast = Color(0xFFFFFFFF)
val SurfaceDarkHighContrast = Color(0xFF1A1C1E)
val OnSurfaceDarkHighContrast = Color(0xFFFFFFFF)
