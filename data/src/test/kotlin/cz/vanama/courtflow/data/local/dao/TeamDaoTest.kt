package cz.vanama.courtflow.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.data.local.entity.TeamEntity
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
class TeamDaoTest {
    private lateinit var database: CourtFlowDatabase
    private lateinit var dao: TeamDao

    @Before
    fun setup() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    CourtFlowDatabase::class.java,
                ).build()
        dao = database.teamDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getAll returns inserted teams ordered by id`() =
        runTest {
            dao.insertAll(listOf(teamEntity(id = 14), teamEntity(id = 1)))

            assertEquals(listOf(1, 14), dao.getAll().map { it.id })
        }

    @Test
    fun `getById returns the matching team or null`() =
        runTest {
            dao.insertAll(listOf(teamEntity(id = 1)))

            assertEquals(1, dao.getById(1)?.id)
            assertNull(dao.getById(99))
        }

    @Test
    fun `insertAll replaces an existing team with the same id`() =
        runTest {
            dao.insertAll(listOf(teamEntity(id = 1)))
            dao.insertAll(listOf(teamEntity(id = 1, name = "Updated")))

            val teams = dao.getAll()

            assertEquals(1, teams.size)
            assertEquals("Updated", teams[0].name)
        }

    private fun teamEntity(
        id: Int,
        name: String = "Hawks",
    ) = TeamEntity(
        id = id,
        abbreviation = "ATL",
        city = "Atlanta",
        conference = "East",
        division = "Southeast",
        fullName = "Atlanta Hawks",
        name = name,
    )
}
