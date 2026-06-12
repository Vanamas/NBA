package cz.vanama.courtflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row caching one NBA team. Populated only by the full team-list sync,
 * so a non-empty table always represents the complete (30-row) list.
 */
@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: Int,
    val abbreviation: String,
    val city: String,
    val conference: String,
    val division: String,
    val fullName: String,
    val name: String,
)
