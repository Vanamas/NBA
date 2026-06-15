package cz.vanama.courtflow.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerFilterTest {
    @Test
    fun `default filter is empty`() {
        assertEquals(true, PlayerFilter().isEmpty())
    }

    @Test
    fun `blank query keeps the filter empty`() {
        assertEquals(true, PlayerFilter(query = "   ").isEmpty())
    }

    @Test
    fun `a search query makes the filter non-empty`() {
        assertEquals(false, PlayerFilter(query = "curry").isEmpty())
    }

    @Test
    fun `a team id makes the filter non-empty`() {
        assertEquals(false, PlayerFilter(teamId = 14).isEmpty())
    }

    @Test
    fun `a position makes the filter non-empty`() {
        assertEquals(false, PlayerFilter(position = "G").isEmpty())
    }
}
