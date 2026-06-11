package cz.vanama.courtflow.domain.usecase

import androidx.paging.PagingData
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

/**
 * Provides the paginated stream of all NBA players for the list screen.
 *
 * Pages of 35 records are loaded lazily as the user scrolls.
 */
class GetPlayersUseCase(
    private val playerRepository: PlayerRepository,
) {
    /** Returns a cold [Flow] of player pages backed by the remote API. */
    operator fun invoke(): Flow<PagingData<Player>> = playerRepository.getPlayers()
}
