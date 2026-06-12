package cz.vanama.courtflow.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import cz.vanama.courtflow.core.network.api.BallDontLieApi
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.data.repository.toDataException
import cz.vanama.courtflow.domain.model.Player
import retrofit2.HttpException
import java.io.IOException

/**
 * Paging 3 source loading players from the balldontlie API.
 *
 * The API uses cursor-based pagination: the key is the `next_cursor`
 * value returned by the previous page, `null` for the first page.
 */
class PlayerPagingSource(
    private val api: BallDontLieApi,
    private val search: String? = null,
) : PagingSource<Int, Player>() {
    override fun getRefreshKey(state: PagingState<Int, Player>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Player> =
        try {
            val cursor = params.key
            val response =
                api.getPlayers(
                    cursor = cursor,
                    perPage = params.loadSize.coerceAtMost(MAX_PAGE_SIZE),
                    search = search,
                )

            LoadResult.Page(
                data = response.data.map { it.toDomain() },
                prevKey = null, // The API uses cursor-based pagination, prevKey is tricky without more info
                nextKey = response.meta?.nextCursor,
            )
        } catch (e: IOException) {
            LoadResult.Error(e.toDataException())
        } catch (e: HttpException) {
            LoadResult.Error(e.toDataException())
        }

    private companion object {
        /** The API rejects `per_page` above 100 with HTTP 400. */
        const val MAX_PAGE_SIZE = 100
    }
}
