package cz.vanama.courtflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.vanama.courtflow.data.local.entity.CacheMetadataEntity

/** Access to per-resource cache freshness timestamps. */
@Dao
interface CacheMetadataDao {
    /** The stored timestamp for [resourceKey], or `null` if never fetched. */
    @Query("SELECT * FROM cache_metadata WHERE resourceKey = :resourceKey")
    suspend fun get(resourceKey: String): CacheMetadataEntity?

    /** Inserts or replaces [entity], updating the timestamp for its resource key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CacheMetadataEntity)
}
