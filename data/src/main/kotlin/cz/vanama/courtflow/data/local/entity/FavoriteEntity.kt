package cz.vanama.courtflow.data.local.entity

import androidx.room.Entity

/**
 * Room row marking one entity as a user favorite. The composite primary key
 * `(id, type)` lets a player and a team share a numeric id without colliding.
 * [type] stores a `FavoriteType` name; [addedAt] is `System.currentTimeMillis()`
 * at insertion and drives newest-first ordering.
 */
@Entity(tableName = "favorites", primaryKeys = ["id", "type"])
data class FavoriteEntity(
    val id: Int,
    val type: String,
    val addedAt: Long,
)
