package cz.vanama.courtflow.core.designsystem.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceholderImagesTest {
    @Test
    fun `playerPortrait builds pollinations url seeded by player id`() {
        val url = PlaceholderImages.playerPortrait(playerId = 19)

        assertTrue(url.startsWith("https://image.pollinations.ai/prompt/"))
        assertTrue(url.contains("seed=19"))
        assertTrue(url.contains("width=512"))
        assertTrue(url.contains("height=512"))
        assertTrue(url.contains("nologo=true"))
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
    fun `teamEmblem builds pollinations url seeded by team id`() {
        val url = PlaceholderImages.teamEmblem(teamId = 10)

        assertTrue(url.startsWith("https://image.pollinations.ai/prompt/"))
        assertTrue(url.contains("seed=10"))
        assertTrue(url.contains("nologo=true"))
        assertFalse("URL must not contain raw spaces", url.contains(" "))
    }

    @Test
    fun `player and team urls differ for the same id`() {
        assertFalse(
            PlaceholderImages.playerPortrait(playerId = 5) == PlaceholderImages.teamEmblem(teamId = 5),
        )
    }
}
