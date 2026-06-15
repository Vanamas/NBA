package cz.vanama.courtflow.domain.usecase

import app.cash.turbine.test
import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.repository.FavoritesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class IsFavoriteUseCaseTest {
    private val repository = mockk<FavoritesRepository>()
    private val useCase = IsFavoriteUseCase(repository)

    @Test
    fun `forwards the repository flow for the given id and type`() =
        runTest {
            every { repository.isFavorite(10, FavoriteType.TEAM) } returns flowOf(false, true)

            useCase(10, FavoriteType.TEAM).test {
                assertEquals(false, awaitItem())
                assertEquals(true, awaitItem())
                awaitComplete()
            }
        }
}
