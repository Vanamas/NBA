package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.core.network.generated.model.NBATeam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TeamMapperTest {
    @Test
    fun `toDomain maps all team attributes`() {
        val dto =
            NBATeam(
                id = 10,
                abbreviation = "GSW",
                city = "Golden State",
                conference = NBATeam.Conference.West,
                division = NBATeam.Division.Pacific,
                fullName = "Golden State Warriors",
                name = "Warriors",
            )

        val team = dto.toDomain()

        assertEquals(10, team.id)
        assertEquals("GSW", team.abbreviation)
        assertEquals("Golden State", team.city)
        assertEquals("West", team.conference)
        assertEquals("Pacific", team.division)
        assertEquals("Golden State Warriors", team.fullName)
        assertEquals("Warriors", team.name)
    }

    @Test
    fun `toDomain maps East conference to its wire value`() {
        val team = NBATeam(id = 1, conference = NBATeam.Conference.East).toDomain()

        assertEquals("East", team.conference)
    }

    @Test
    fun `toDomain falls back to empty strings for missing text and enum attributes`() {
        val team = NBATeam(id = 10).toDomain()

        assertEquals("", team.abbreviation)
        assertEquals("", team.city)
        assertEquals("", team.conference)
        assertEquals("", team.division)
        assertEquals("", team.fullName)
        assertEquals("", team.name)
    }

    @Test
    fun `toDomain fails when id is missing`() {
        val dto = NBATeam(fullName = "Golden State Warriors")

        val e = assertThrows(IllegalArgumentException::class.java) { dto.toDomain() }

        assertEquals("Team is missing an id", e.message)
    }
}
