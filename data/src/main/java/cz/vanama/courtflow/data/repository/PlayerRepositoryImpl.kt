package cz.vanama.courtflow.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import cz.vanama.courtflow.core.network.api.BallDontLieApi
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.data.paging.PlayerPagingSource
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

/**
 * [PlayerRepository] backed by the balldontlie REST API.
 */
class PlayerRepositoryImpl(
    private val api: BallDontLieApi,
) : PlayerRepository {
    override fun getPlayers(): Flow<PagingData<Player>> =
        Pager(
            config =
                PagingConfig(
                    pageSize = 35,
                    enablePlaceholders = false,
                ),
            pagingSourceFactory = { PlayerPagingSource(api) },
        ).flow

    override suspend fun getPlayerById(id: Int): Player = safeApiCall { api.getPlayer(id).data.toDomain() }
}
