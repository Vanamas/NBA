package cz.vanama.courtflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import cz.vanama.courtflow.data.local.dao.PlayerDao
import cz.vanama.courtflow.data.local.dao.RemoteKeyDao
import cz.vanama.courtflow.data.local.dao.TeamDao
import cz.vanama.courtflow.data.local.entity.PlayerEntity
import cz.vanama.courtflow.data.local.entity.RemoteKeyEntity
import cz.vanama.courtflow.data.local.entity.TeamEntity

/**
 * Offline cache of balldontlie API data. Every row can be refetched from the
 * network, so the schema is not exported and migrations may be destructive.
 */
@Database(
    entities = [PlayerEntity::class, TeamEntity::class, RemoteKeyEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CourtFlowDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    abstract fun teamDao(): TeamDao

    abstract fun remoteKeyDao(): RemoteKeyDao

    companion object {
        /** On-device database file name. */
        const val NAME = "courtflow.db"
    }
}
