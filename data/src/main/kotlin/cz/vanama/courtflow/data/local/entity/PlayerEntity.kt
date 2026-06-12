package cz.vanama.courtflow.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row caching one player of the unfiltered list.
 *
 * The player's team is embedded with a `team_` column prefix instead of a
 * foreign key: the API always inlines the full team object and the list
 * screen needs the club name in the same query, so a JOIN buys nothing.
 */
@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: Int,
    val firstName: String,
    val lastName: String,
    val position: String,
    val height: String?,
    val weight: String?,
    val jerseyNumber: String?,
    val college: String?,
    val country: String?,
    val draftYear: Int?,
    val draftRound: Int?,
    val draftNumber: Int?,
    @Embedded(prefix = "team_") val team: TeamEntity,
)
