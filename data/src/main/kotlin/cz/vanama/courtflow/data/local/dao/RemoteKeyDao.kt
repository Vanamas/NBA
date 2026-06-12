package cz.vanama.courtflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.vanama.courtflow.data.local.entity.RemoteKeyEntity

/** Access to the single pagination cursor of the unfiltered player list. */
@Dao
interface RemoteKeyDao {
    /** The stored cursor row, or `null` before the first successful refresh. */
    @Query("SELECT * FROM remote_keys LIMIT 1")
    suspend fun get(): RemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: RemoteKeyEntity)

    @Query("DELETE FROM remote_keys")
    suspend fun clear()
}
