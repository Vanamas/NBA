package cz.vanama.courtflow.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceholderImagesTest {
    @Test
    fun `playerPortrait builds dicebear url seeded by player id`() {
        val url = PlaceholderImages.playerPortrait(playerId = 19)

        assertTrue(url.startsWith("https://api.dicebear.com/9.x/avataaars/png"))
        assertTrue(url.contains("seed=19"))
        assertTrue(url.contains("size=512"))
        assertFalse("URL must not contain raw spaces", url.contains(" "))
    }

    @Test
    fun `playerPortrait is deterministic for the same player`() {
        assertEquals(
            PlaceholderImages.playerPortrait(playerId = 19),
            PlaceholderImages.playerPortrait(playerId = 19),
        )
    }

    @Test
    fun `teamEmblem builds dicebear url seeded by team id`() {
        val url = PlaceholderImages.teamEmblem(teamId = 10)

        assertTrue(url.startsWith("https://api.dicebear.com/9.x/shapes/png"))
        assertTrue(url.contains("seed=10"))
        assertFalse("URL must not contain raw spaces", url.contains(" "))
    }

    @Test
    fun `player and team urls differ for the same id`() {
        assertFalse(
            PlaceholderImages.playerPortrait(playerId = 5) == PlaceholderImages.teamEmblem(teamId = 5),
        )
    }
}
