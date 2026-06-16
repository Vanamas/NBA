package cz.vanama.courtflow.domain.repository

import cz.vanama.courtflow.domain.model.Team
import kotlinx.coroutines.flow.Flow

/**
 * Access to NBA team data; implemented in the data layer on top of the remote API.
 */
interface TeamRepository {
    /**
     * Returns all NBA teams. Serves the offline cache while it is fresh;
     * [forceRefresh] = `true` (pull-to-refresh) reloads from the network
     * regardless of the cache age.
     */
    fun getTeams(forceRefresh: Boolean = false): Flow<List<Team>>

    /** Fetches a single team by its [id]. */
    suspend fun getTeamById(id: Int): Team
}
