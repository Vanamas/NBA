package cz.vanama.courtflow.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.data.cache.CacheKeys
import cz.vanama.courtflow.data.cache.CachePolicy
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.data.local.entity.CacheMetadataEntity
import cz.vanama.courtflow.data.local.entity.PlayerEntity
import cz.vanama.courtflow.data.local.entity.RemoteKeyEntity
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.data.mapper.toEntity
import cz.vanama.courtflow.data.repository.safeApiCall

/**
 * Streams the unfiltered player list from the balldontlie API into
 * [CourtFlowDatabase], so Paging 3 pages from Room and the list works
 * offline.
 *
 * The API paginates with an opaque, forward-only `next_cursor`, so a single
 * remote-key row is enough: REFRESH reloads from the first page and replaces
 * the whole cache in one transaction, APPEND continues from the stored
 * cursor, and PREPEND never loads (there is nothing before the first page).
 *
 * [initialize] skips the initial refresh while the persisted `cache_metadata`
 * timestamp is within [CachePolicy.TTL]; stale or absent metadata triggers a
 * full reload. An explicit pull-to-refresh bypasses this and always reloads.
 */
@OptIn(ExperimentalPagingApi::class)
class PlayerRemoteMediator(
    private val api: NBAApi,
    private val database: CourtFlowDatabase,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : RemoteMediator<Int, PlayerEntity>() {
    override suspend fun initialize(): InitializeAction {
        val lastFetchedAt = database.cacheMetadataDao().get(CacheKeys.PLAYERS)?.lastFetchedAt
        return if (CachePolicy.isStale(lastFetchedAt, nowMillis())) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PlayerEntity>,
    ): MediatorResult =
        when (loadType) {
            LoadType.REFRESH ->
                fetchAndStore(
                    cursor = null,
                    perPage = state.config.initialLoadSize,
                    clearExisting = true,
                )
            LoadType.PREPEND -> MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> append(state)
        }

    private suspend fun append(state: PagingState<Int, PlayerEntity>): MediatorResult {
        val remoteKey = database.remoteKeyDao().get()
        val cursor = remoteKey?.nextCursor
        return if (cursor == null) {
            // A missing row means REFRESH has not completed yet; a stored
            // null cursor means the API reported the end of the list.
            MediatorResult.Success(endOfPaginationReached = remoteKey != null)
        } else {
            fetchAndStore(cursor = cursor, perPage = state.config.pageSize, clearExisting = false)
        }
    }

    private suspend fun fetchAndStore(
        cursor: Int?,
        perPage: Int,
        clearExisting: Boolean,
    ): MediatorResult =
        try {
            val (players, nextCursor) =
                safeApiCall {
                    val response =
                        api.nbaV1PlayersGet(
                            cursor = cursor,
                            perPage = perPage.coerceAtMost(MAX_PAGE_SIZE),
                        )
                    response.data.orEmpty().map { it.toDomain().toEntity() } to response.meta?.nextCursor
                }
            database.withTransaction {
                if (clearExisting) {
                    database.playerDao().clearAll()
                    database.remoteKeyDao().clear()
                }
                database.remoteKeyDao().insert(RemoteKeyEntity(nextCursor = nextCursor))
                database.playerDao().insertAll(players)
                if (clearExisting) {
                    database.cacheMetadataDao().upsert(
                        CacheMetadataEntity(
                            resourceKey = CacheKeys.PLAYERS,
                            lastFetchedAt = nowMillis(),
                        ),
                    )
                }
            }
            MediatorResult.Success(endOfPaginationReached = nextCursor == null)
        } catch (e: DataException) {
            MediatorResult.Error(e)
        }

    private companion object {
        /**
         * The API rejects `per_page` above 100 with HTTP 400. [PlayerPagingSource]
         * keeps an identical private constant; duplicated here rather than widening
         * its visibility for the sake of a single value.
         */
        const val MAX_PAGE_SIZE = 100
    }
}
