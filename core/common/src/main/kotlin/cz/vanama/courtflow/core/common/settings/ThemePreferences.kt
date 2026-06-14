package cz.vanama.courtflow.core.common.settings

/**
 * Persisted appearance preferences. [dynamicColor] opts into the Material You
 * wallpaper palette on Android 12+; [themeMode] overrides (or follows) the
 * system dark-mode setting; [trueBlack] swaps the dark scheme for a pure-black
 * AMOLED variant. Defaults preserve the app's pre-settings behaviour: dynamic
 * color on, follow the system for dark/light, no AMOLED.
 */
data class ThemePreferences(
    val dynamicColor: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val trueBlack: Boolean = false,
)
