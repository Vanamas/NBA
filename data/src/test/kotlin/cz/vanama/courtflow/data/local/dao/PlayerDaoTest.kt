package cz.vanama.courtflow.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.data.local.entity.PlayerEntity
import cz.vanama.courtflow.data.local.entity.TeamEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlayerDaoTest {
    private lateinit var database: CourtFlowDatabase
    private lateinit var dao: PlayerDao

    @Before
    fun setup() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    CourtFlowDatabase::class.java,
                ).build()
        dao = database.playerDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `pagingSource serves inserted players ordered by id with the embedded team intact`() =
        runTest {
            val second = playerEntity(id = 2)
            dao.insertAll(listOf(second, playerEntity(id = 1)))

            val page =
                dao.pagingSource().load(
                    PagingSource.LoadParams.Refresh(key = null, loadSize = 35, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page

            assertEquals(listOf(1, 2), page.data.map { it.id })
            assertEquals(second, page.data[1])
        }

    @Test
    fun `insertAll replaces an existing player with the same id`() =
        runTest {
            dao.insertAll(listOf(playerEntity(id = 1)))
            dao.insertAll(listOf(playerEntity(id = 1, firstName = "Updated")))

            val players = dao.getAll()

            assertEquals(1, players.size)
            assertEquals("Updated", players[0].firstName)
        }

    @Test
    fun `clearAll empties the table`() =
        runTest {
            dao.insertAll(listOf(playerEntity(id = 1), playerEntity(id = 2)))

            dao.clearAll()

            assertEquals(emptyList<PlayerEntity>(), dao.getAll())
        }

    @Test
    fun `getById returns the matching player or null`() =
        runTest {
            dao.insertAll(listOf(playerEntity(id = 1), playerEntity(id = 2)))

            assertEquals(2, dao.getById(2)?.id)
            assertEquals(null, dao.getById(404))
        }

    private fun playerEntity(
        id: Int,
        firstName: String = "LeBron",
    ) = PlayerEntity(
        id = id,
        firstName = firstName,
        lastName = "James",
        position = "F",
        height = "6-9",
        weight = "250",
        jerseyNumber = "23",
        college = null,
        country = "USA",
        draftYear = 2003,
        draftRound = 1,
        draftNumber = 1,
        team =
            TeamEntity(
                id = 14,
                abbreviation = "LAL",
                city = "Los Angeles",
                conference = "West",
                division = "Pacific",
                fullName = "Los Angeles Lakers",
                name = "Lakers",
            ),
    )
}
