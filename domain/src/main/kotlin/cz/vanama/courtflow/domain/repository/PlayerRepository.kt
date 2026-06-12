package cz.vanama.courtflow.domain.repository

import androidx.paging.PagingData
import cz.vanama.courtflow.domain.model.Player
import kotlinx.coroutines.flow.Flow

/**
 * Access to NBA player data; implemented in the data layer on top of the remote API.
 */
interface PlayerRepository {
    /** Returns a paginated stream of players, optionally filtered by [query]. */
    fun getPlayers(query: String? = null): Flow<PagingData<Player>>

    /** Fetches a single player by its [id]. */
    suspend fun getPlayerById(id: Int): Player
}
