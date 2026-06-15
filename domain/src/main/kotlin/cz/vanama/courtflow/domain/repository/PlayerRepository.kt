package cz.vanama.courtflow.domain.repository

import androidx.paging.PagingData
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.PlayerFilter
import kotlinx.coroutines.flow.Flow

/**
 * Access to NBA player data; implemented in the data layer on top of the remote API.
 */
interface PlayerRepository {
    /** Returns a paginated stream of players, optionally filtered by [query]. */
    fun getPlayers(query: String? = null): Flow<PagingData<Player>>

    /**
     * Returns a paginated stream of players narrowed by [filter]. An empty
     * filter is served offline-first from the cache; any active search, team
     * or position routes through the network-only paging path.
     */
    fun getPlayers(filter: PlayerFilter): Flow<PagingData<Player>>

    /** Returns a paginated stream of the players of the team with [teamId]. */
    fun getTeamPlayers(teamId: Int): Flow<PagingData<Player>>

    /** Fetches a single player by its [id]. */
    suspend fun getPlayerById(id: Int): Player
}
