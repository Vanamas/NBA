package cz.vanama.courtflow.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cz.vanama.courtflow.data.cache.CacheKeys
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.data.local.entity.CacheMetadataEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CacheMetadataDaoTest {
    private lateinit var database: CourtFlowDatabase
    private lateinit var dao: CacheMetadataDao

    @Before
    fun setup() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    CourtFlowDatabase::class.java,
                ).build()
        dao = database.cacheMetadataDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `get returns null for an unknown resource key`() =
        runTest {
            assertNull(dao.get(CacheKeys.PLAYERS))
        }

    @Test
    fun `upsert stores a timestamp that get returns`() =
        runTest {
            dao.upsert(CacheMetadataEntity(resourceKey = CacheKeys.PLAYERS, lastFetchedAt = 1_000L))

            assertEquals(1_000L, dao.get(CacheKeys.PLAYERS)?.lastFetchedAt)
        }

    @Test
    fun `upsert replaces the timestamp for the same key`() =
        runTest {
            dao.upsert(CacheMetadataEntity(resourceKey = CacheKeys.PLAYERS, lastFetchedAt = 1_000L))
            dao.upsert(CacheMetadataEntity(resourceKey = CacheKeys.PLAYERS, lastFetchedAt = 2_000L))

            assertEquals(2_000L, dao.get(CacheKeys.PLAYERS)?.lastFetchedAt)
        }

    @Test
    fun `get for one key is unaffected by an upsert for a different key`() =
        runTest {
            dao.upsert(CacheMetadataEntity(resourceKey = CacheKeys.PLAYERS, lastFetchedAt = 1_000L))

            assertNull(dao.get("unrelated"))
            assertEquals(1_000L, dao.get(CacheKeys.PLAYERS)?.lastFetchedAt)
        }
}
