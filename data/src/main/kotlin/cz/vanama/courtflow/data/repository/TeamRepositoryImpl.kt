package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.data.cache.CacheKeys
import cz.vanama.courtflow.data.cache.CachePolicy
import cz.vanama.courtflow.data.local.dao.CacheMetadataDao
import cz.vanama.courtflow.data.local.dao.TeamDao
import cz.vanama.courtflow.data.local.entity.CacheMetadataEntity
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.data.mapper.toEntity
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * [TeamRepository] backed by the balldontlie REST API with a Room
 * read-through cache.
 *
 * The team list (30 static rows) is offline-first: it is served from Room
 * while the cached `teams` timestamp is within [CachePolicy.TTL]. Once stale
 * (or on an explicit `forceRefresh`) it is refetched and restamped; if that
 * refetch fails but a cache exists, the stale list is served instead of an
 * error. [getTeamById] is a read-through that shares the `teams` freshness
 * window: it serves the cached row while the list timestamp is fresh, otherwise
 * refetches the single team and upserts its row (without restamping `teams`).
 * The list ([getTeams]) is gated by the `teams` timestamp rather than by row
 * presence, so a partially filled table from detail fetches is never mistaken
 * for a complete, fresh list.
 * [nowMillis] is injectable for deterministic tests.
 */
class TeamRepositoryImpl(
    private val api: NBAApi,
    private val teamDao: TeamDao,
    private val cacheMetadataDao: CacheMetadataDao,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : TeamRepository {
    override fun getTeams(forceRefresh: Boolean): Flow<List<Team>> =
        flow {
            val cached = teamDao.getAll()
            val lastFetchedAt = cacheMetadataDao.get(CacheKeys.TEAMS)?.lastFetchedAt
            val fresh = cached.isNotEmpty() && !CachePolicy.isStale(lastFetchedAt, nowMillis())
            if (fresh && !forceRefresh) {
                emit(cached.map { it.toDomain() })
                return@flow
            }
            try {
                val teams =
                    safeApiCall {
                        api
                            .nbaV1TeamsGet()
                            .data
                            .orEmpty()
                            .map { it.toDomain() }
                    }
                teamDao.insertAll(teams.map { it.toEntity() })
                cacheMetadataDao.upsert(CacheMetadataEntity(CacheKeys.TEAMS, nowMillis()))
                emit(teams)
            } catch (e: DataException) {
                // Offline-first: a non-empty cache outlives a failed refresh.
                if (cached.isNotEmpty()) emit(cached.map { it.toDomain() }) else throw e
            }
        }

    override suspend fun getTeamById(id: Int): Team {
        val cached = teamDao.getById(id)
        val lastFetchedAt = cacheMetadataDao.get(CacheKeys.TEAMS)?.lastFetchedAt
        if (cached != null && !CachePolicy.isStale(lastFetchedAt, nowMillis())) {
            return cached.toDomain()
        }
        return try {
            val team =
                safeApiCall {
                    requireNotNull(api.nbaV1TeamsIdGet(id).data) { "Team $id missing in response" }.toDomain()
                }
            teamDao.insertAll(listOf(team.toEntity()))
            team
        } catch (e: DataException) {
            // Offline-first: a cached team outlives a failed refresh.
            cached?.toDomain() ?: throw e
        }
    }
}
