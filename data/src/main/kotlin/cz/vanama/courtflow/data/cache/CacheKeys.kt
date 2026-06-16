package cz.vanama.courtflow.data.cache

/**
 * Logical cache-resource identifiers stored in `cache_metadata.resourceKey`.
 * Centralised so no raw strings leak into mediators or repositories.
 */
object CacheKeys {
    /** The unfiltered player list. */
    const val PLAYERS = "players"
}
