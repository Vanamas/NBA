package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.core.network.generated.model.NBAGame
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GameMapperTest {
    private val warriorsDto =
        NBATeam(
            id = 10,
            abbreviation = "GSW",
            city = "Golden State",
            conference = NBATeam.Conference.West,
            division = NBATeam.Division.Pacific,
            fullName = "Golden State Warriors",
            name = "Warriors",
        )

    private val lakersDto =
        NBATeam(
            id = 14,
            abbreviation = "LAL",
            city = "Los Angeles",
            conference = NBATeam.Conference.West,
            division = NBATeam.Division.Pacific,
            fullName = "Los Angeles Lakers",
            name = "Lakers",
        )

    @Test
    fun `toDomain maps all game attributes`() {
        val dto =
            NBAGame(
                id = 1,
                date = "2026-06-10",
                season = 2025,
                status = "Final",
                homeTeamScore = 112,
                visitorTeamScore = 99,
                homeTeam = warriorsDto,
                visitorTeam = lakersDto,
            )

        val game = dto.toDomain()

        assertEquals(1, game.id)
        assertEquals("2026-06-10", game.date)
        assertEquals("Final", game.status)
        assertEquals("GSW", game.homeTeam.abbreviation)
        assertEquals(112, game.homeTeamScore)
        assertEquals("LAL", game.visitorTeam.abbreviation)
        assertEquals(99, game.visitorTeamScore)
    }

    @Test
    fun `toDomain falls back to empty texts and zero scores`() {
        val dto =
            NBAGame(
                id = 1,
                date = null,
                status = null,
                homeTeamScore = null,
                visitorTeamScore = null,
                homeTeam = warriorsDto,
                visitorTeam = lakersDto,
            )

        val game = dto.toDomain()

        assertEquals("", game.date)
        assertEquals("", game.status)
        assertEquals(0, game.homeTeamScore)
        assertEquals(0, game.visitorTeamScore)
    }

    @Test
    fun `toDomain rejects a game without an id`() {
        val dto = NBAGame(id = null, homeTeam = warriorsDto, visitorTeam = lakersDto)

        assertThrows(IllegalArgumentException::class.java) { dto.toDomain() }
    }

    @Test
    fun `toDomain rejects a game without both teams`() {
        val noHome = NBAGame(id = 1, homeTeam = null, visitorTeam = lakersDto)
        val noVisitor = NBAGame(id = 1, homeTeam = warriorsDto, visitorTeam = null)

        assertThrows(IllegalArgumentException::class.java) { noHome.toDomain() }
        assertThrows(IllegalArgumentException::class.java) { noVisitor.toDomain() }
    }
}
