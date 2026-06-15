package cz.vanama.courtflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.vanama.courtflow.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

/** Access to the user's favorite players and teams. */
@Dao
interface FavoriteDao {
    /** Emits whether the entity ([id], [type]) is a favorite, re-emitting on every change. */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id AND type = :type)")
    fun isFavorite(
        id: Int,
        type: String,
    ): Flow<Boolean>

    /** Emits the ids of all favorites of [type], newest first; re-emits on every change. */
    @Query("SELECT id FROM favorites WHERE type = :type ORDER BY addedAt DESC")
    fun observeIds(type: String): Flow<List<Int>>

    /** One-shot check used by the toggle to decide between insert and delete. */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id AND type = :type)")
    suspend fun exists(
        id: Int,
        type: String,
    ): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id AND type = :type")
    suspend fun delete(
        id: Int,
        type: String,
    )
}
