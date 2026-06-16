package cz.vanama.courtflow.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.vanama.courtflow.data.local.entity.PlayerEntity

/** Access to the cached unfiltered player list. */
@Dao
interface PlayerDao {
    /**
     * Pages the cached players in API order — the balldontlie cursor walks
     * player ids ascending, so `ORDER BY id` reproduces the remote order.
     */
    @Query("SELECT * FROM players ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, PlayerEntity>

    /** Snapshot of every cached player in paging order. */
    @Query("SELECT * FROM players ORDER BY id ASC")
    suspend fun getAll(): List<PlayerEntity>

    /** The cached player with [id], or `null` if it is not cached. */
    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getById(id: Int): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<PlayerEntity>)

    @Query("DELETE FROM players")
    suspend fun clearAll()
}
