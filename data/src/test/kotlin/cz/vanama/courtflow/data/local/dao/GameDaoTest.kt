package cz.vanama.courtflow.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.data.local.entity.GameEntity
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
class GameDaoTest {
    private lateinit var database: CourtFlowDatabase
    private lateinit var dao: GameDao

    @Before
    fun setup() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    CourtFlowDatabase::class.java,
                ).build()
        dao = database.gameDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getByTeamId returns the team's games newest first`() =
        runTest {
            dao.insertAll(
                listOf(
                    gameEntity(teamId = 10, id = 1, date = "2026-06-01"),
                    gameEntity(teamId = 10, id = 2, date = "2026-06-09"),
                ),
            )

            assertEquals(listOf(2, 1), dao.getByTeamId(10).map { it.id })
        }

    @Test
    fun `replaceForTeam swaps only the given team's games`() =
        runTest {
            dao.insertAll(listOf(gameEntity(teamId = 10, id = 1), gameEntity(teamId = 14, id = 9)))

            dao.replaceForTeam(10, listOf(gameEntity(teamId = 10, id = 2)))

            assertEquals(listOf(2), dao.getByTeamId(10).map { it.id })
            assertEquals(listOf(9), dao.getByTeamId(14).map { it.id })
        }

    @Test
    fun `getByTeamId is empty for an unknown team`() =
        runTest {
            dao.insertAll(listOf(gameEntity(teamId = 10, id = 1)))

            assertEquals(emptyList<Int>(), dao.getByTeamId(14).map { it.id })
        }

    @Test
    fun `insertAll replaces a game with the same teamId and id`() =
        runTest {
            dao.insertAll(listOf(gameEntity(teamId = 10, id = 1).copy(homeTeamScore = 100)))
            dao.insertAll(listOf(gameEntity(teamId = 10, id = 1).copy(homeTeamScore = 120)))

            val games = dao.getByTeamId(10)

            assertEquals(1, games.size)
            assertEquals(120, games[0].homeTeamScore)
        }

    private fun gameEntity(
        teamId: Int,
        id: Int,
        date: String = "2026-06-01",
    ) = GameEntity(
        teamId = teamId,
        id = id,
        date = date,
        homeTeam = team(10, "Golden State Warriors"),
        homeTeamScore = 112,
        visitorTeam = team(14, "Los Angeles Lakers"),
        visitorTeamScore = 99,
    )

    private fun team(
        id: Int,
        fullName: String,
    ) = TeamEntity(
        id = id,
        abbreviation = "ABC",
        city = "City",
        conference = "West",
        division = "Pacific",
        fullName = fullName,
        name = "Name",
    )
}
