package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.repository.FavoritesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleFavoriteUseCaseTest {
    private val repository = mockk<FavoritesRepository>(relaxed = true)
    private val useCase = ToggleFavoriteUseCase(repository)

    @Test
    fun `delegates to repository with the given id and type`() =
        runTest {
            coEvery { repository.toggle(19, FavoriteType.PLAYER) } returns Unit

            useCase(19, FavoriteType.PLAYER)

            coVerify(exactly = 1) { repository.toggle(19, FavoriteType.PLAYER) }
        }
}
