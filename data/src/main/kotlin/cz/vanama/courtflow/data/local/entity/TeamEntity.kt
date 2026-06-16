package cz.vanama.courtflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row caching one NBA team. Populated by the full team-list sync and by
 * single-team detail fetches; the list's freshness is gated by the `teams`
 * cache_metadata timestamp, not by row presence, so a partially filled table
 * is never mistaken for a complete, fresh list.
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
