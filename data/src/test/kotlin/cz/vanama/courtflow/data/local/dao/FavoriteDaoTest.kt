package cz.vanama.courtflow.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.data.local.entity.FavoriteEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FavoriteDaoTest {
    private lateinit var database: CourtFlowDatabase
    private lateinit var dao: FavoriteDao

    @Before
    fun setup() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    CourtFlowDatabase::class.java,
                ).build()
        dao = database.favoriteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `isFavorite reflects insert and delete`() =
        runTest {
            dao.isFavorite(19, "PLAYER").test {
                assertFalse(awaitItem())

                dao.insert(FavoriteEntity(id = 19, type = "PLAYER", addedAt = 1L))
                assertTrue(awaitItem())

                dao.delete(19, "PLAYER")
                assertFalse(awaitItem())
            }
        }

    @Test
    fun `exists distinguishes id and type`() =
        runTest {
            dao.insert(FavoriteEntity(id = 19, type = "PLAYER", addedAt = 1L))

            assertTrue(dao.exists(19, "PLAYER"))
            assertFalse(dao.exists(19, "TEAM"))
            assertFalse(dao.exists(21, "PLAYER"))
        }

    @Test
    fun `observeIds returns ids of the type newest first`() =
        runTest {
            dao.insert(FavoriteEntity(id = 19, type = "PLAYER", addedAt = 1L))
            dao.insert(FavoriteEntity(id = 21, type = "PLAYER", addedAt = 2L))
            dao.insert(FavoriteEntity(id = 10, type = "TEAM", addedAt = 3L))

            dao.observeIds("PLAYER").test {
                assertEquals(listOf(21, 19), awaitItem())
            }
        }
}
