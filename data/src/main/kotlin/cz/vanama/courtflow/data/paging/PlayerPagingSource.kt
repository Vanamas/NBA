package cz.vanama.courtflow.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.squareup.moshi.JsonDataException
import cz.vanama.courtflow.core.network.generated.api.NBAApi
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
 *
 * Every failure — transport ([IOException], [HttpException]), Moshi
 * deserialization ([JsonDataException]) and mapper invariant violations
 * ([IllegalArgumentException]) — is returned as [LoadResult.Error] carrying
 * a classified domain exception. Paging 3.5 does NOT catch throwables
 * escaping [load] (verified in `PageFetcherSnapshot`), so anything uncaught
 * here would crash the app.
 */
class PlayerPagingSource(
    private val api: NBAApi,
    private val search: String? = null,
    private val teamIds: List<Int>? = null,
) : PagingSource<Int, Player>() {
    /**
     * The cursor is opaque, so no arithmetic on it can be correct. The
     * closest page's `prevKey` is always `null` here, which restarts a
     * refresh from the first page — the only safe option for the API's
     * forward-only cursors.
     */
    override fun getRefreshKey(state: PagingState<Int, Player>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Player> =
        try {
            val cursor = params.key
            val response =
                api.nbaV1PlayersGet(
                    cursor = cursor,
                    perPage = params.loadSize.coerceAtMost(MAX_PAGE_SIZE),
                    search = search,
                    teamIds = teamIds,
                )

            LoadResult.Page(
                data = response.data.orEmpty().map { it.toDomain() },
                prevKey = null, // The API uses cursor-based pagination, prevKey is tricky without more info
                nextKey = response.meta?.nextCursor,
            )
        } catch (e: IOException) {
            LoadResult.Error(e.toDataException())
        } catch (e: HttpException) {
            LoadResult.Error(e.toDataException())
        } catch (e: JsonDataException) {
            LoadResult.Error(e.toDataException())
        } catch (e: IllegalArgumentException) {
            LoadResult.Error(e.toDataException())
        }

    private companion object {
        /** The API rejects `per_page` above 100 with HTTP 400. */
        const val MAX_PAGE_SIZE = 100
    }
}
