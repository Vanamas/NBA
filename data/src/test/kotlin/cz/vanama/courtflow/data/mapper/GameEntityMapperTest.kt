package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.domain.model.Game
import cz.vanama.courtflow.domain.model.Team
import org.junit.Assert.assertEquals
import org.junit.Test

class GameEntityMapperTest {
    private val home =
        Team(
            id = 10,
            abbreviation = "GSW",
            city = "Golden State",
            conference = "West",
            division = "Pacific",
            fullName = "Golden State Warriors",
            name = "Warriors",
            imageUrl = "https://api.dicebear.com/9.x/shapes/png?seed=10&size=512",
        )
    private val visitor =
        Team(
            id = 14,
            abbreviation = "LAL",
            city = "Los Angeles",
            conference = "West",
            division = "Pacific",
            fullName = "Los Angeles Lakers",
            name = "Lakers",
            imageUrl = "https://api.dicebear.com/9.x/shapes/png?seed=14&size=512",
        )
    private val game =
        Game(
            id = 7,
            date = "2026-06-09",
            homeTeam = home,
            homeTeamScore = 112,
            visitorTeam = visitor,
            visitorTeamScore = 99,
        )

    @Test
    fun `toEntity carries the owning team id and the game fields`() {
        val entity = game.toEntity(teamId = 10)

        assertEquals(10, entity.teamId)
        assertEquals(7, entity.id)
        assertEquals("2026-06-09", entity.date)
        assertEquals("Golden State Warriors", entity.homeTeam.fullName)
        assertEquals(112, entity.homeTeamScore)
        assertEquals("Los Angeles Lakers", entity.visitorTeam.fullName)
        assertEquals(99, entity.visitorTeamScore)
    }

    @Test
    fun `toDomain round-trips the game fields`() {
        assertEquals(game, game.toEntity(teamId = 10).toDomain())
    }
}
