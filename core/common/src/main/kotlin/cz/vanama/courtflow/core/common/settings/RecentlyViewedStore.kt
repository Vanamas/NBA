package cz.vanama.courtflow.core.common.settings

import kotlinx.coroutines.flow.Flow

/**
 * Persists the ids of the players the user most recently opened, newest first,
 * de-duplicated and capped at [MAX_RECENTLY_VIEWED]. A local key-value settings
 * store (DataStore-backed), not a domain repository — a cross-cutting
 * `core:common` concern, deliberately kept out of the `domain`/`data` layers.
 */
interface RecentlyViewedStore {
    /** Ordered list of recently viewed player ids, newest first. */
    val recentlyViewedIds: Flow<List<Int>>

    /**
     * Records [playerId] as the most recently viewed: moves it to the front,
     * removing any earlier occurrence, and trims to [MAX_RECENTLY_VIEWED].
     */
    suspend fun recordView(playerId: Int)

    companion object {
        /** Upper bound on the stored history; the strip shows at most this many. */
        const val MAX_RECENTLY_VIEWED = 10
    }
}
