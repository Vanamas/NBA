package cz.vanama.courtflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks when each logical cache resource was last successfully fetched, so
 * [cz.vanama.courtflow.data.cache.CachePolicy] can decide whether to refetch.
 * Keyed by a stable resource string (see
 * [cz.vanama.courtflow.data.cache.CacheKeys]).
 */
@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey val resourceKey: String,
    val lastFetchedAt: Long,
)
