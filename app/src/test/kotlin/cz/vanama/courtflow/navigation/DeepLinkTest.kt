package cz.vanama.courtflow.navigation

import android.net.Uri
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DeepLinkTest {
    @After
    fun tearDown() {
        // CourtFlowApplication (instantiated per test by Robolectric) starts Koin;
        // stop it so the next test's Application.onCreate can start it again.
        stopKoin()
    }

    @Test
    fun `player uri maps to PlayerDetail`() {
        assertEquals(Destination.PlayerDetail(19), DeepLink.parse(Uri.parse("courtflow://player/19")))
    }

    @Test
    fun `team uri maps to TeamDetail`() {
        assertEquals(Destination.TeamDetail(10), DeepLink.parse(Uri.parse("courtflow://team/10")))
    }

    @Test
    fun `players uri maps to PlayerList`() {
        assertEquals(Destination.PlayerList, DeepLink.parse(Uri.parse("courtflow://players")))
    }

    @Test
    fun `teams uri maps to TeamList`() {
        assertEquals(Destination.TeamList, DeepLink.parse(Uri.parse("courtflow://teams")))
    }

    @Test
    fun `settings uri maps to Settings`() {
        assertEquals(Destination.Settings, DeepLink.parse(Uri.parse("courtflow://settings")))
    }

    @Test
    fun `list uris with extra path segments are rejected`() {
        assertNull(DeepLink.parse(Uri.parse("courtflow://players/19")))
        assertNull(DeepLink.parse(Uri.parse("courtflow://teams/extra")))
        assertNull(DeepLink.parse(Uri.parse("courtflow://settings/extra")))
    }

    @Test
    fun `null, foreign scheme and malformed ids are rejected`() {
        assertNull(DeepLink.parse(null))
        assertNull(DeepLink.parse(Uri.parse("https://player/19")))
        assertNull(DeepLink.parse(Uri.parse("courtflow://player/abc")))
        assertNull(DeepLink.parse(Uri.parse("courtflow://player")))
        assertNull(DeepLink.parse(Uri.parse("courtflow://unknown/1")))
    }

    @Test
    fun `initial back stack for a detail uri keeps the player list beneath`() {
        assertEquals(
            listOf(Destination.PlayerList, Destination.TeamDetail(10)),
            DeepLink.initialBackStack(Uri.parse("courtflow://team/10")),
        )
    }

    @Test
    fun `initial back stack for the teams uri keeps the player list beneath`() {
        assertEquals(
            listOf(Destination.PlayerList, Destination.TeamList),
            DeepLink.initialBackStack(Uri.parse("courtflow://teams")),
        )
    }

    @Test
    fun `initial back stack for the settings uri keeps the player list beneath`() {
        assertEquals(
            listOf(Destination.PlayerList, Destination.Settings),
            DeepLink.initialBackStack(Uri.parse("courtflow://settings")),
        )
    }

    @Test
    fun `initial back stack for the players uri does not duplicate the root`() {
        assertEquals(
            listOf(Destination.PlayerList),
            DeepLink.initialBackStack(Uri.parse("courtflow://players")),
        )
    }

    @Test
    fun `initial back stack without a deep link is just the player list`() {
        assertEquals(listOf(Destination.PlayerList), DeepLink.initialBackStack(null))
    }
}
