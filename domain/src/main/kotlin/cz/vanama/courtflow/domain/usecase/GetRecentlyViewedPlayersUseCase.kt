package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.core.common.settings.RecentlyViewedStore
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Observes the recently viewed player ids and resolves them to full [Player]s,
 * newest first. Ids that fail to resolve are skipped so a single transient
 * failure never empties the strip; the relative order of the survivors is kept.
 */
class GetRecentlyViewedPlayersUseCase(
    private val recentlyViewedStore: RecentlyViewedStore,
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(): Flow<List<Player>> =
        recentlyViewedStore.recentlyViewedIds.map { ids ->
            ids.mapNotNull { id ->
                runCatching { playerRepository.getPlayerById(id) }.getOrNull()
            }
        }
}
