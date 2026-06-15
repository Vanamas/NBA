package cz.vanama.courtflow.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteTypeTest {
    @Test
    fun `enum exposes exactly player and team`() {
        assertEquals(
            listOf("PLAYER", "TEAM"),
            FavoriteType.entries.map { it.name },
        )
    }
}
