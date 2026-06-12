package cz.vanama.courtflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.vanama.courtflow.data.local.entity.TeamEntity

/** Access to the cached NBA teams. */
@Dao
interface TeamDao {
    /** All cached teams in API order (id ascending); empty until the first successful sync. */
    @Query("SELECT * FROM teams ORDER BY id ASC")
    suspend fun getAll(): List<TeamEntity>

    /** The cached team with [id], or `null` when it has not been synced yet. */
    @Query("SELECT * FROM teams WHERE id = :id")
    suspend fun getById(id: Int): TeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teams: List<TeamEntity>)
}
