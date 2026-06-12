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
    fun `list uris with extra path segments are rejected`() {
        assertNull(DeepLink.parse(Uri.parse("courtflow://players/19")))
        assertNull(DeepLink.parse(Uri.parse("courtflow://teams/extra")))
    }

    @Test
    fun `null, foreign scheme and malformed ids are rejected`() {
        assertNull(DeepLink.parse(null))
        assertNull(DeepLink.parse(Uri.parse("https://player/19")))
        assertNull(DeepLink.parse(Uri.parse("courtflow://player/abc")))
        assertNull(DeepLink.parse(Uri.parse("courtflow://player")))
        assertNull(DeepLink.parse(Uri.parse("courtflow://unknown/1")))
    }
}
