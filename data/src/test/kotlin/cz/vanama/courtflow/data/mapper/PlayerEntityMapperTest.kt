package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlayerEntityMapperTest {
    private val warriors =
        Team(
            id = 10,
            abbreviation = "GSW",
            city = "Golden State",
            conference = "West",
            division = "Pacific",
            fullName = "Golden State Warriors",
            name = "Warriors",
        )

    @Test
    fun `domain player round-trips through the entity`() {
        val player =
            Player(
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
                team = warriors,
            )

        assertEquals(player, player.toEntity().toDomain())
    }

    @Test
    fun `optional attributes survive the round trip as null`() {
        val player =
            Player(
                id = 1,
                firstName = "LeBron",
                lastName = "James",
                position = "F",
                team = warriors,
            )

        val entity = player.toEntity()

        assertNull(entity.height)
        assertEquals(player, entity.toDomain())
    }
}
