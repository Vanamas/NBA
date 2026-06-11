package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.repository.PlayerRepository

/**
 * Loads the full detail of a single player.
 */
class GetPlayerDetailUseCase(
    private val playerRepository: PlayerRepository
) {
    /** Fetches the player with the given [id]; throws when the request fails. */
    suspend operator fun invoke(id: Int): Player {
        return playerRepository.getPlayerById(id)
    }
}
