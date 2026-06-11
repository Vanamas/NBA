package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.repository.TeamRepository

/**
 * Loads the full detail of a single team.
 */
class GetTeamDetailUseCase(
    private val teamRepository: TeamRepository
) {
    /** Fetches the team with the given [id]; throws when the request fails. */
    suspend operator fun invoke(id: Int): Team {
        return teamRepository.getTeamById(id)
    }
}
