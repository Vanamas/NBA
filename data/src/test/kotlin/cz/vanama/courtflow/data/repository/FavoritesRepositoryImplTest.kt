package cz.vanama.courtflow.data.repository

import app.cash.turbine.test
import cz.vanama.courtflow.data.local.dao.FavoriteDao
import cz.vanama.courtflow.data.local.entity.FavoriteEntity
import cz.vanama.courtflow.domain.model.FavoriteType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoritesRepositoryImplTest {
    private val dao = mockk<FavoriteDao>(relaxed = true)
    private val repository = FavoritesRepositoryImpl(dao)

    @Test
    fun `isFavorite maps the type name and forwards the dao flow`() =
        runTest {
            every { dao.isFavorite(19, "PLAYER") } returns flowOf(true)

            repository.isFavorite(19, FavoriteType.PLAYER).test {
                assertEquals(true, awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `observeFavorites maps the type name and forwards the dao flow`() =
        runTest {
            every { dao.observeIds("TEAM") } returns flowOf(listOf(10, 14))

            repository.observeFavorites(FavoriteType.TEAM).test {
                assertEquals(listOf(10, 14), awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `toggle inserts when the entity is not yet a favorite`() =
        runTest {
            coEvery { dao.exists(19, "PLAYER") } returns false
            val inserted = slot<FavoriteEntity>()
            coEvery { dao.insert(capture(inserted)) } returns Unit

            repository.toggle(19, FavoriteType.PLAYER)

            assertEquals(19, inserted.captured.id)
            assertEquals("PLAYER", inserted.captured.type)
            coVerify(exactly = 0) { dao.delete(any(), any()) }
        }

    @Test
    fun `toggle deletes when the entity is already a favorite`() =
        runTest {
            coEvery { dao.exists(10, "TEAM") } returns true

            repository.toggle(10, FavoriteType.TEAM)

            coVerify(exactly = 1) { dao.delete(10, "TEAM") }
            coVerify(exactly = 0) { dao.insert(any()) }
        }
}
