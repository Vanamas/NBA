package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.core.network.generated.model.NBAStandings
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StandingMapperTest {
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

    @Test
    fun `toDomain maps all standing attributes`() {
        val dto =
            NBAStandings(
                team = warriorsDto,
                wins = 12,
                losses = 4,
                conferenceRank = 3,
                conferenceRecord = "8-2",
                divisionRank = 1,
                season = 2025,
            )

        val standing = dto.toDomain()

        assertEquals(10, standing.teamId)
        assertEquals(12, standing.wins)
        assertEquals(4, standing.losses)
        assertEquals(3, standing.conferenceRank)
        assertEquals("West", standing.conference)
    }

    @Test
    fun `toDomain falls back to zero numbers and empty conference`() {
        val teamNoConference = warriorsDto.copy(conference = null)
        val dto =
            NBAStandings(
                team = teamNoConference,
                wins = null,
                losses = null,
                conferenceRank = null,
            )

        val standing = dto.toDomain()

        assertEquals(0, standing.wins)
        assertEquals(0, standing.losses)
        assertEquals(0, standing.conferenceRank)
        assertEquals("", standing.conference)
    }

    @Test
    fun `toDomain rejects a standing without a team`() {
        val dto = NBAStandings(team = null, wins = 12, losses = 4, conferenceRank = 3)

        assertThrows(IllegalArgumentException::class.java) { dto.toDomain() }
    }
}
