package cz.vanama.courtflow.core.common.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [ThemePreferencesRepository] backed by a Preferences [DataStore]. */
class DataStoreThemePreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : ThemePreferencesRepository {
    override val themePreferences: Flow<ThemePreferences> =
        dataStore.data.map { prefs ->
            ThemePreferences(
                dynamicColor = prefs[DYNAMIC_COLOR] ?: ThemePreferences().dynamicColor,
                themeMode = prefs[THEME_MODE].toThemeMode(),
                trueBlack = prefs[TRUE_BLACK] ?: ThemePreferences().trueBlack,
            )
        }

    override suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    override suspend fun setTrueBlack(enabled: Boolean) {
        dataStore.edit { it[TRUE_BLACK] = enabled }
    }

    /** Test-only seam to write an arbitrary raw mode string (forward-compat check). */
    internal suspend fun setThemeModeRaw(raw: String) {
        dataStore.edit { it[THEME_MODE] = raw }
    }

    private fun String?.toThemeMode(): ThemeMode = ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.SYSTEM

    private companion object {
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val TRUE_BLACK = booleanPreferencesKey("true_black")
    }
}
