package cz.vanama.courtflow.core.common.settings

import kotlinx.coroutines.flow.Flow

/**
 * Reads and persists the user's [ThemePreferences]. A local key-value settings
 * store (DataStore-backed), not a domain repository — it is a cross-cutting
 * `core:common` concern, deliberately kept out of the `domain`/`data` layers.
 */
interface ThemePreferencesStore {
    val themePreferences: Flow<ThemePreferences>

    suspend fun setDynamicColor(enabled: Boolean)

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setTrueBlack(enabled: Boolean)
}
