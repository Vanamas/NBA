package cz.vanama.courtflow.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity

/**
 * Room row caching one of a team's recent games. Keyed by `(teamId, id)` —
 * [teamId] is the team whose recent-games list this row belongs to, so the
 * same game can be cached under both its teams. Both clubs are embedded with
 * column prefixes (the API inlines them and the detail screen renders both),
 * mirroring [PlayerEntity]'s embedded team.
 */
@Entity(tableName = "games", primaryKeys = ["teamId", "id"])
data class GameEntity(
    val teamId: Int,
    val id: Int,
    val date: String,
    @Embedded(prefix = "home_") val homeTeam: TeamEntity,
    val homeTeamScore: Int,
    @Embedded(prefix = "visitor_") val visitorTeam: TeamEntity,
    val visitorTeamScore: Int,
)
