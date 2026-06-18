package cz.vanama.courtflow.domain.usecase

import androidx.paging.PagingData
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.PlayerFilter
import cz.vanama.courtflow.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

/**
 * Provides the paginated stream of NBA players for the list screen, narrowed
 * by an optional [PlayerFilter] (search query, team and position).
 *
 * Pages of 35 records are loaded lazily as the user scrolls.
 */
class GetPlayersUseCase(
    private val playerRepository: PlayerRepository,
) {
    /** Returns a cold [Flow] of player pages narrowed by [filter]. */
    operator fun invoke(filter: PlayerFilter = PlayerFilter()): Flow<PagingData<Player>> =
        playerRepository.getPlayers(filter)
}
