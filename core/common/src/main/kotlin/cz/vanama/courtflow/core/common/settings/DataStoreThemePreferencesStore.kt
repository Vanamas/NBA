package cz.vanama.courtflow.core.common.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** [ThemePreferencesStore] backed by a Preferences [DataStore]. */
class DataStoreThemePreferencesStore(
    private val dataStore: DataStore<Preferences>,
) : ThemePreferencesStore {
    override val themePreferences: Flow<ThemePreferences> =
        dataStore.data
            // A Preferences DataStore surfaces a read failure (corruption, restored
            // backup, disk error) as an IOException on this flow. Without this the
            // throwable would propagate into the app-root collector and crash the UI;
            // fall back to defaults instead and rethrow anything unexpected.
            .catch { cause ->
                if (cause is IOException) emit(emptyPreferences()) else throw cause
            }.map { prefs ->
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
