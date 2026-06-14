package cz.vanama.courtflow.feature.settings

import cz.vanama.courtflow.core.common.settings.ThemeMode

/** UI state of the settings screen, mirrored from the persisted preferences. */
data class SettingsState(
    val dynamicColor: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val trueBlack: Boolean = false,
)

/** User actions of the settings screen. */
sealed class SettingsIntent {
    data class OnDynamicColorChanged(
        val enabled: Boolean,
    ) : SettingsIntent()

    data class OnThemeModeChanged(
        val mode: ThemeMode,
    ) : SettingsIntent()

    data class OnTrueBlackChanged(
        val enabled: Boolean,
    ) : SettingsIntent()
}
