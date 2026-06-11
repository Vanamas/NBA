package cz.vanama.courtflow.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import cz.vanama.courtflow.core.network.api.BallDontLieApi
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.domain.model.Player

/**
 * Paging 3 source loading players from the balldontlie API.
 *
 * The API uses cursor-based pagination: the key is the `next_cursor`
 * value returned by the previous page, `null` for the first page.
 */
class PlayerPagingSource(
    private val api: BallDontLieApi
) : PagingSource<Int, Player>() {

    override fun getRefreshKey(state: PagingState<Int, Player>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Player> {
        return try {
            val cursor = params.key
            val response = api.getPlayers(cursor = cursor, perPage = params.loadSize)
            
            LoadResult.Page(
                data = response.data.map { it.toDomain() },
                prevKey = null, // The API uses cursor-based pagination, prevKey is tricky without more info
                nextKey = response.meta?.nextCursor
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
