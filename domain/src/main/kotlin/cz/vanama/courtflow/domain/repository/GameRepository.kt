package cz.vanama.courtflow.domain.repository

import cz.vanama.courtflow.domain.model.Game

/**
 * Access to NBA game data; implemented in the data layer on top of the remote API.
 */
interface GameRepository {
    /**
     * Returns the most recently completed games of the team with [teamId],
     * newest first; an empty list when the team has not played recently.
     */
    suspend fun getRecentGames(teamId: Int): List<Game>
}
