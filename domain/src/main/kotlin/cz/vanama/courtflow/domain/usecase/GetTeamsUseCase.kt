package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow

/**
 * Provides the list of all NBA teams for the team list screen.
 */
class GetTeamsUseCase(
    private val teamRepository: TeamRepository,
) {
    /**
     * Returns a cold [Flow] emitting the full team list. [forceRefresh] = `true`
     * bypasses the cache TTL and reloads from the network.
     */
    operator fun invoke(forceRefresh: Boolean = false): Flow<List<Team>> = teamRepository.getTeams(forceRefresh)
}
