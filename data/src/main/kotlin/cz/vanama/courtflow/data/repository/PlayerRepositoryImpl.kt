package cz.vanama.courtflow.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.data.paging.PlayerPagingSource
import cz.vanama.courtflow.data.paging.PlayerRemoteMediator
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.PlayerFilter
import cz.vanama.courtflow.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [PlayerRepository] backed by the balldontlie REST API.
 *
 * The unfiltered player list is offline-first: [PlayerRemoteMediator] syncs
 * API pages into [CourtFlowDatabase] and Paging 3 pages from Room, so the
 * list works without a connection and survives process death. Search results
 * and team rosters stay network-only via [PlayerPagingSource] — caching
 * arbitrary query results would need per-query tables and remote keys for no
 * real offline value.
 */
class PlayerRepositoryImpl(
    private val api: NBAApi,
    private val database: CourtFlowDatabase,
) : PlayerRepository {
    @OptIn(ExperimentalPagingApi::class)
    override fun getPlayers(query: String?): Flow<PagingData<Player>> =
        if (query.isNullOrBlank()) {
            Pager(
                config = playerPagingConfig(),
                remoteMediator = PlayerRemoteMediator(api, database),
                pagingSourceFactory = { database.playerDao().pagingSource() },
            ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
        } else {
            Pager(
                config = playerPagingConfig(),
                pagingSourceFactory = { PlayerPagingSource(api, search = query) },
            ).flow
        }

    override fun getPlayers(filter: PlayerFilter): Flow<PagingData<Player>> {
        if (filter.isEmpty()) {
            return getPlayers(query = null)
        }
        val teamIds = filter.teamId?.let { listOf(it) }
        val search = filter.query.ifBlank { null }
        val stream =
            Pager(
                config = playerPagingConfig(),
                pagingSourceFactory = {
                    PlayerPagingSource(api, search = search, teamIds = teamIds)
                },
            ).flow
        // /players has no position parameter, so it is enforced client-side
        // over each loaded page. This filters within the loaded pages only;
        // a page can therefore yield fewer than 35 visible items, but Paging
        // keeps appending pages as the user scrolls, so the stream stays whole.
        val position = filter.position
        return if (position == null) {
            stream
        } else {
            stream.map { pagingData -> pagingData.filter { it.position == position } }
        }
    }

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
