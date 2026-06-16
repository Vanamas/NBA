package cz.vanama.courtflow.data.cache

/**
 * Logical cache-resource identifiers stored in `cache_metadata.resourceKey`.
 * Centralised so no raw strings leak into mediators or repositories.
 */
object CacheKeys {
    /** The unfiltered player list. */
    const val PLAYERS = "players"

    /** The full NBA team list. */
    const val TEAMS = "teams"

    /** A single player's detail, by id. */
    fun player(id: Int): String = "player:$id"

    /** A single team's recent games, by team id. */
    fun games(teamId: Int): String = "games:$teamId"
}
