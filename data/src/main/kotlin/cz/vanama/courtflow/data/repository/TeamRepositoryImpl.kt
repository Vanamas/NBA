package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.data.local.dao.TeamDao
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
 * The team list (30 static rows) is persisted on the first successful fetch,
 * so repeated visits and team details work offline across process restarts
 * and redundant calls against the API rate limit are avoided. Only [getTeams]
 * populates the table — caching single [getTeamById] results would make a
 * partially filled table masquerade as the complete list.
 */
class TeamRepositoryImpl(
    private val api: NBAApi,
    private val teamDao: TeamDao,
) : TeamRepository {
    override fun getTeams(): Flow<List<Team>> =
        flow {
            val cached = teamDao.getAll()
            if (cached.isNotEmpty()) {
                emit(cached.map { it.toDomain() })
                return@flow
            }
            val teams =
                safeApiCall {
                    api.nbaV1TeamsGet().data.orEmpty().map { it.toDomain() }
                }
            teamDao.insertAll(teams.map { it.toEntity() })
            emit(teams)
        }

    override suspend fun getTeamById(id: Int): Team =
        teamDao.getById(id)?.toDomain()
            ?: safeApiCall {
                requireNotNull(api.nbaV1TeamsIdGet(id).data) { "Team $id missing in response" }.toDomain()
            }
}
