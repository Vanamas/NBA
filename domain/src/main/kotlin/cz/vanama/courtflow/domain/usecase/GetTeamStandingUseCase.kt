package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.Standing
import cz.vanama.courtflow.domain.repository.StandingsRepository

/**
 * Loads the current-season standing of a single team for the team detail
 * screen.
 */
class GetTeamStandingUseCase(
    private val standingsRepository: StandingsRepository,
) {
    /** Fetches the standing of the team with [teamId]; `null` when none is published yet. */
    suspend operator fun invoke(teamId: Int): Standing? = standingsRepository.getTeamStanding(teamId)
}
