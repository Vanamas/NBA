package cz.vanama.courtflow.data.paging

import androidx.paging.PagingSource
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.core.network.generated.model.NbaV1PlayersGet200Response
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerPagingSourceTest {
    private val api: NBAApi = mockk()

    @Test
    fun `load clamps the requested page size to the API maximum of 100`() =
        runTest {
            coEvery {
                api.nbaV1PlayersGet(cursor = null, perPage = 100, search = null)
            } returns NbaV1PlayersGet200Response(data = emptyList())
            val source = PlayerPagingSource(api)

            val result =
                source.load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = 105,
                        placeholdersEnabled = false,
                    ),
                )

            assertTrue(result is PagingSource.LoadResult.Page)
            coVerify(exactly = 1) { api.nbaV1PlayersGet(cursor = null, perPage = 100, search = null) }
        }
}
