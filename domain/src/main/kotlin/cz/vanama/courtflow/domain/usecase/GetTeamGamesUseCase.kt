package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.Game
import cz.vanama.courtflow.domain.repository.GameRepository

/**
 * Loads the recently completed games of a single team for the team detail screen.
 */
class GetTeamGamesUseCase(
    private val gameRepository: GameRepository,
) {
    /** Fetches the recent games of the team with [teamId], newest first; throws when the request fails. */
    suspend operator fun invoke(teamId: Int): List<Game> = gameRepository.getRecentGames(teamId)
}
