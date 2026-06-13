package cz.vanama.courtflow.core.common.settings

import kotlinx.coroutines.flow.Flow

/** Reads and persists the user's [ThemePreferences]. */
interface ThemePreferencesRepository {
    val themePreferences: Flow<ThemePreferences>

    suspend fun setDynamicColor(enabled: Boolean)

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setTrueBlack(enabled: Boolean)
}
