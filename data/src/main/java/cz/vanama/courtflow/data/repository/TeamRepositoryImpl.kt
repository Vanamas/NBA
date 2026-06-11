package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.core.network.api.BallDontLieApi
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * [TeamRepository] backed by the balldontlie REST API.
 */
class TeamRepositoryImpl(
    private val api: BallDontLieApi,
) : TeamRepository {
    override fun getTeams(): Flow<List<Team>> =
        flow {
            val response = safeApiCall { api.getTeams() }
            emit(response.data.map { it.toDomain() })
        }

    override suspend fun getTeamById(id: Int): Team = safeApiCall { api.getTeam(id).data.toDomain() }
}
