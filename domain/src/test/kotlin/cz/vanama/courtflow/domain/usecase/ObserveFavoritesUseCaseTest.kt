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

class ObserveFavoritesUseCaseTest {
    private val repository = mockk<FavoritesRepository>()
    private val useCase = ObserveFavoritesUseCase(repository)

    @Test
    fun `forwards the repository favorites flow for the given type`() =
        runTest {
            every { repository.observeFavorites(FavoriteType.PLAYER) } returns flowOf(listOf(19, 21))

            useCase(FavoriteType.PLAYER).test {
                assertEquals(listOf(19, 21), awaitItem())
                awaitComplete()
            }
        }
}
