package cz.vanama.courtflow.core.common.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import cz.vanama.courtflow.core.common.settings.RecentlyViewedStore.Companion.MAX_RECENTLY_VIEWED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** [RecentlyViewedStore] backed by a Preferences [DataStore]. */
class DataStoreRecentlyViewedStore(
    private val dataStore: DataStore<Preferences>,
) : RecentlyViewedStore {
    override val recentlyViewedIds: Flow<List<Int>> =
        dataStore.data
            // A Preferences DataStore surfaces a read failure (corruption, restored
            // backup, disk error) as an IOException on this flow. Without this the
            // throwable would propagate into the app-root collector and crash the UI;
            // fall back to an empty history instead and rethrow anything unexpected.
            .catch { cause ->
                if (cause is IOException) emit(emptyPreferences()) else throw cause
            }.map { prefs -> prefs[RECENT_IDS].toIdList() }

    override suspend fun recordView(playerId: Int) {
        dataStore.edit { prefs ->
            val updated =
                (listOf(playerId) + prefs[RECENT_IDS].toIdList().filterNot { it == playerId })
                    .take(MAX_RECENTLY_VIEWED)
            prefs[RECENT_IDS] = updated.joinToString(separator = ",")
        }
    }

    private fun String?.toIdList(): List<Int> =
        this
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            .orEmpty()

    private companion object {
        val RECENT_IDS = stringPreferencesKey("recently_viewed_ids")
    }
}
