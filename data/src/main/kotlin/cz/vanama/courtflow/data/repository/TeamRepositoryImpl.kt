package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.core.network.api.BallDontLieApi
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicReference

/**
 * [TeamRepository] backed by the balldontlie REST API.
 *
 * The team list (30 static rows) is cached in memory for the lifetime of
 * the process, so repeated visits and team details work offline once the
 * list has been fetched, and redundant calls against the API rate limit
 * are avoided. The bound Koin definition is a `single`, so the cache is
 * shared app-wide.
 */
class TeamRepositoryImpl(
    private val api: BallDontLieApi,
) : TeamRepository {
    private val cachedTeams = AtomicReference<List<Team>?>(null)

    override fun getTeams(): Flow<List<Team>> =
        flow {
            cachedTeams.get()?.let {
                emit(it)
                return@flow
            }
            val response = safeApiCall { api.getTeams() }
            val teams = response.data.map { it.toDomain() }
            cachedTeams.set(teams)
            emit(teams)
        }

    override suspend fun getTeamById(id: Int): Team =
        cachedTeams.get()?.firstOrNull { it.id == id }
            ?: safeApiCall { api.getTeam(id).data.toDomain() }
}
