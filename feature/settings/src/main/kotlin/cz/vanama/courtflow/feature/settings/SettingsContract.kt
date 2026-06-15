package cz.vanama.courtflow.feature.settings

import cz.vanama.courtflow.core.common.settings.ThemeMode

/**
 * UI state of the settings screen, mirrored from the persisted theme preferences
 * plus the per-app language. [currentLanguageTag] is `""` when the app follows
 * the system language; [languageTags] is the set of translations the app ships.
 */
data class SettingsState(
    val dynamicColor: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val trueBlack: Boolean = false,
    val currentLanguageTag: String = "",
    val languageTags: List<String> = emptyList(),
    val versionName: String = "",
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

    data class OnLanguageSelected(
        val tag: String,
    ) : SettingsIntent()

    /** The user tapped the "Open-source licenses" row. */
    data object OnOssLicensesClicked : SettingsIntent()
}

/** One-shot events emitted by [SettingsViewModel]. */
sealed class SettingsEffect {
    /** A preference could not be persisted (DataStore write failure); shown as a snackbar. */
    data object PreferenceWriteFailed : SettingsEffect()

    /** Launch the generated open-source-licenses screen. */
    data object OpenOssLicenses : SettingsEffect()
}
