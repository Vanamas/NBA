package cz.vanama.courtflow.domain.usecase

import androidx.paging.PagingData
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

/**
 * Provides the paginated roster of a single team for the team detail screen.
 */
class GetTeamPlayersUseCase(
    private val playerRepository: PlayerRepository,
) {
    /** Returns a cold [Flow] of the pages of players of the team with [teamId]. */
    operator fun invoke(teamId: Int): Flow<PagingData<Player>> = playerRepository.getTeamPlayers(teamId)
}
