package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.domain.model.Team
import org.junit.Assert.assertEquals
import org.junit.Test

class TeamEntityMapperTest {
    @Test
    fun `domain team round-trips through the entity`() {
        val team =
            Team(
                id = 10,
                abbreviation = "GSW",
                city = "Golden State",
                conference = "West",
                division = "Pacific",
                fullName = "Golden State Warriors",
                name = "Warriors",
            )

        assertEquals(team, team.toEntity().toDomain())
    }
}
