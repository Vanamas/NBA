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
    /** Returns a cold [Flow] emitting the full team list from the remote API. */
    operator fun invoke(): Flow<List<Team>> = teamRepository.getTeams()
}
