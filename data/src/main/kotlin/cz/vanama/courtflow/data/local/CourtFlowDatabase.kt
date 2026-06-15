package cz.vanama.courtflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import cz.vanama.courtflow.data.local.dao.FavoriteDao
import cz.vanama.courtflow.data.local.dao.PlayerDao
import cz.vanama.courtflow.data.local.dao.RemoteKeyDao
import cz.vanama.courtflow.data.local.dao.TeamDao
import cz.vanama.courtflow.data.local.entity.FavoriteEntity
import cz.vanama.courtflow.data.local.entity.PlayerEntity
import cz.vanama.courtflow.data.local.entity.RemoteKeyEntity
import cz.vanama.courtflow.data.local.entity.TeamEntity

/**
 * Offline cache of balldontlie API data plus the user's favorites. Cache rows
 * can be refetched from the network and favorites are user re-creatable, so
 * the schema is not exported and migrations may be destructive.
 */
@Database(
    entities = [PlayerEntity::class, TeamEntity::class, RemoteKeyEntity::class, FavoriteEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class CourtFlowDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    abstract fun teamDao(): TeamDao

    abstract fun remoteKeyDao(): RemoteKeyDao

    abstract fun favoriteDao(): FavoriteDao

    companion object {
        /** On-device database file name. */
        const val NAME = "courtflow.db"
    }
}
