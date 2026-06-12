package cz.vanama.courtflow.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.data.paging.PlayerPagingSource
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

/**
 * [PlayerRepository] backed by the balldontlie REST API.
 */
class PlayerRepositoryImpl(
    private val api: NBAApi,
) : PlayerRepository {
    override fun getPlayers(query: String?): Flow<PagingData<Player>> =
        Pager(
            config = playerPagingConfig(),
            pagingSourceFactory = { PlayerPagingSource(api, search = query) },
        ).flow

    override fun getTeamPlayers(teamId: Int): Flow<PagingData<Player>> =
        Pager(
            config = playerPagingConfig(),
            pagingSourceFactory = { PlayerPagingSource(api, teamIds = listOf(teamId)) },
        ).flow

    private fun playerPagingConfig() =
        PagingConfig(
            pageSize = 35,
            // Default initialLoadSize is 3 × pageSize = 105, which the API
            // rejects (per_page max 100); the assignment wants 35 per page.
            initialLoadSize = 35,
            enablePlaceholders = false,
        )

    override suspend fun getPlayerById(id: Int): Player =
        safeApiCall {
            requireNotNull(api.nbaV1PlayersIdGet(id).data) { "Player $id missing in response" }.toDomain()
        }
}
