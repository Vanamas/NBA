package cz.vanama.courtflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import cz.vanama.courtflow.data.local.entity.GameEntity

/** Access to the cached recent games, grouped by the team whose list they belong to. */
@Dao
interface GameDao {
    /** A team's cached games, newest first (ISO `date` strings sort correctly). */
    @Query("SELECT * FROM games WHERE teamId = :teamId ORDER BY date DESC")
    suspend fun getByTeamId(teamId: Int): List<GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<GameEntity>)

    @Query("DELETE FROM games WHERE teamId = :teamId")
    suspend fun deleteByTeamId(teamId: Int)

    /** Atomically replaces a team's cached games with [games]. */
    @Transaction
    suspend fun replaceForTeam(
        teamId: Int,
        games: List<GameEntity>,
    ) {
        deleteByTeamId(teamId)
        insertAll(games)
    }
}
