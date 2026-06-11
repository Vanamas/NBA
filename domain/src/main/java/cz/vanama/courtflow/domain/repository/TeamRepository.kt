package cz.vanama.courtflow.domain.repository

import cz.vanama.courtflow.domain.model.Team
import kotlinx.coroutines.flow.Flow

/**
 * Access to NBA team data; implemented in the data layer on top of the remote API.
 */
interface TeamRepository {
    /** Returns all NBA teams. */
    fun getTeams(): Flow<List<Team>>

    /** Fetches a single team by its [id]. */
    suspend fun getTeamById(id: Int): Team
}
