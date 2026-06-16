package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.data.cache.CacheKeys
import cz.vanama.courtflow.data.cache.CachePolicy
import cz.vanama.courtflow.data.local.dao.CacheMetadataDao
import cz.vanama.courtflow.data.local.dao.GameDao
import cz.vanama.courtflow.data.local.entity.CacheMetadataEntity
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.data.mapper.toEntity
import cz.vanama.courtflow.domain.model.Game
import cz.vanama.courtflow.domain.repository.GameRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * [GameRepository] backed by the balldontlie REST API with a Room read-through
 * cache.
 *
 * "Recent" is a single request limited to a [WINDOW_DAYS]-day window, filtered
 * to completed games and sorted by date client-side. The result (a team's five
 * most recent games — possibly empty in the off-season) is cached per team and
 * served from Room while the `games:{teamId}` timestamp is within
 * [CachePolicy.TTL]: freshness is timestamp-based, so an empty off-season
 * result is served from cache without refetching. A stale/missing entry
 * triggers a refetch; if that fails but a cache exists, the stale games are
 * served. [nowMillis] is injectable for deterministic tests.
 */
class GameRepositoryImpl(
    private val api: NBAApi,
    private val gameDao: GameDao,
    private val cacheMetadataDao: CacheMetadataDao,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : GameRepository {
    override suspend fun getRecentGames(teamId: Int): List<Game> {
        val now = nowMillis()
        val lastFetchedAt = cacheMetadataDao.get(CacheKeys.games(teamId))?.lastFetchedAt
        val cached = gameDao.getByTeamId(teamId)
        if (!CachePolicy.isStale(lastFetchedAt, now)) {
            return cached.map { it.toDomain() }
        }
        return try {
            val games =
                safeApiCall {
                    api
                        .nbaV1GamesGet(
                            perPage = PAGE_SIZE,
                            teamIds = listOf(teamId),
                            startDate = isoDate(now - TimeUnit.DAYS.toMillis(WINDOW_DAYS)),
                            endDate = isoDate(now),
                        ).data
                        .orEmpty()
                        .filter { it.status == STATUS_FINAL }
                        .map { it.toDomain() }
                        .sortedByDescending { it.date }
                        .take(RECENT_GAMES_LIMIT)
                }
            gameDao.replaceForTeam(teamId, games.map { it.toEntity(teamId) })
            cacheMetadataDao.upsert(CacheMetadataEntity(CacheKeys.games(teamId), now))
            games
        } catch (e: DataException) {
            // Offline-first: cached games outlive a failed refresh.
            if (cached.isNotEmpty()) cached.map { it.toDomain() } else throw e
        }
    }

    /** Formats [epochMillis] as the API's `yyyy-MM-dd`, pinned to UTC for determinism. */
    private fun isoDate(epochMillis: Long): String =
        SimpleDateFormat(ISO_DATE_PATTERN, Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(epochMillis))

    private companion object {
        /** One page comfortably covers a team's games within [WINDOW_DAYS] (≤ ~25). */
        const val PAGE_SIZE = 50
        const val RECENT_GAMES_LIMIT = 5
        const val WINDOW_DAYS = 45L
        const val ISO_DATE_PATTERN = "yyyy-MM-dd"

        /** The API's status for completed games; otherwise it carries tip-off time or quarter info. */
        const val STATUS_FINAL = "Final"
    }
}
