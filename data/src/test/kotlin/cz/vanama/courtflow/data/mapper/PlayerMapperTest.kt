package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.core.network.generated.model.NBAPlayer
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class PlayerMapperTest {
    private val teamDto =
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
    fun `toDomain maps all player attributes`() {
        val dto =
            NBAPlayer(
                id = 19,
                firstName = "Stephen",
                lastName = "Curry",
                position = "G",
                height = "6-2",
                weight = "185",
                jerseyNumber = "30",
                college = "Davidson",
                country = "USA",
                draftYear = 2009,
                draftRound = 1,
                draftNumber = 7,
                team = teamDto,
            )

        val player = dto.toDomain()

        assertEquals(19, player.id)
        assertEquals("Stephen", player.firstName)
        assertEquals("Curry", player.lastName)
        assertEquals("G", player.position)
        assertEquals("6-2", player.height)
        assertEquals("185", player.weight)
        assertEquals("30", player.jerseyNumber)
        assertEquals("Davidson", player.college)
        assertEquals("USA", player.country)
        assertEquals(2009, player.draftYear)
        assertEquals(1, player.draftRound)
        assertEquals(7, player.draftNumber)
        assertEquals("Golden State Warriors", player.team.fullName)
    }

    @Test
    fun `toDomain handles missing optional attributes`() {
        val dto =
            NBAPlayer(
                id = 19,
                firstName = "Stephen",
                lastName = "Curry",
                position = "G",
                height = null,
                weight = null,
                jerseyNumber = null,
                college = null,
                country = null,
                draftYear = null,
                draftRound = null,
                draftNumber = null,
                team = teamDto,
            )

        val player = dto.toDomain()

        assertNull(player.height)
        assertNull(player.weight)
        assertNull(player.jerseyNumber)
        assertNull(player.college)
        assertNull(player.country)
        assertNull(player.draftYear)
        assertNull(player.draftRound)
        assertNull(player.draftNumber)
    }

    @Test
    fun `toDomain fails when id is missing`() {
        val dto = NBAPlayer(id = null, team = teamDto)

        val e = assertThrows(IllegalArgumentException::class.java) { dto.toDomain() }

        assertEquals("Player is missing an id", e.message)
    }

    @Test
    fun `toDomain fails when team is missing`() {
        val dto = NBAPlayer(id = 19, team = null)

        val e = assertThrows(IllegalArgumentException::class.java) { dto.toDomain() }

        assertEquals("Player 19 is missing a team", e.message)
    }
}
