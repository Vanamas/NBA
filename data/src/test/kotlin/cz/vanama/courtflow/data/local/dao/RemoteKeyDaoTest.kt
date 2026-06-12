package cz.vanama.courtflow.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.data.local.entity.RemoteKeyEntity
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
class RemoteKeyDaoTest {
    private lateinit var database: CourtFlowDatabase
    private lateinit var dao: RemoteKeyDao

    @Before
    fun setup() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    CourtFlowDatabase::class.java,
                ).build()
        dao = database.remoteKeyDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `get returns null before the first refresh`() =
        runTest {
            assertNull(dao.get())
        }

    @Test
    fun `insert keeps a single row that get returns`() =
        runTest {
            dao.insert(RemoteKeyEntity(nextCursor = 35))
            dao.insert(RemoteKeyEntity(nextCursor = 70))

            assertEquals(70, dao.get()?.nextCursor)
        }

    @Test
    fun `a stored null cursor is distinguishable from no row at all`() =
        runTest {
            dao.insert(RemoteKeyEntity(nextCursor = null))

            val key = dao.get()

            assertEquals(RemoteKeyEntity(nextCursor = null), key)
            assertNull(key?.nextCursor)
        }

    @Test
    fun `clear removes the stored cursor`() =
        runTest {
            dao.insert(RemoteKeyEntity(nextCursor = 35))

            dao.clear()

            assertNull(dao.get())
        }
}
