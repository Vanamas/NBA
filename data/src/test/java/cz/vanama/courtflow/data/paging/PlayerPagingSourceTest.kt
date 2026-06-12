package cz.vanama.courtflow.data.paging

import androidx.paging.PagingSource
import cz.vanama.courtflow.core.network.api.BallDontLieApi
import cz.vanama.courtflow.core.network.model.CommonResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerPagingSourceTest {
    private val api: BallDontLieApi = mockk()

    @Test
    fun `load clamps the requested page size to the API maximum of 100`() =
        runTest {
            coEvery {
                api.getPlayers(cursor = null, perPage = 100, search = null)
            } returns CommonResponse(data = emptyList())
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
            coVerify(exactly = 1) { api.getPlayers(cursor = null, perPage = 100, search = null) }
        }
}
