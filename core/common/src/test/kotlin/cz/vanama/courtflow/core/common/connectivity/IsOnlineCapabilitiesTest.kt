package cz.vanama.courtflow.core.common.connectivity

import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

/**
 * Unit tests for the pure online predicate. No Robolectric / Context: a mocked
 * [NetworkCapabilities] is enough because [AndroidConnectivityObserver.isOnline]
 * is side-effect-free.
 */
class IsOnlineCapabilitiesTest {
    private fun capabilities(
        internet: Boolean,
        validated: Boolean,
    ): NetworkCapabilities =
        mockk {
            every { hasCapability(NET_CAPABILITY_INTERNET) } returns internet
            every { hasCapability(NET_CAPABILITY_VALIDATED) } returns validated
        }

    @Test
    fun `online only when internet and validated`() {
        AndroidConnectivityObserver.isOnline(
            capabilities(internet = true, validated = true),
        ) shouldBe true
    }

    @Test
    fun `offline when validated but not internet`() {
        AndroidConnectivityObserver.isOnline(
            capabilities(internet = false, validated = true),
        ) shouldBe false
    }

    @Test
    fun `offline when internet but not validated (captive portal)`() {
        AndroidConnectivityObserver.isOnline(
            capabilities(internet = true, validated = false),
        ) shouldBe false
    }

    @Test
    fun `offline when neither capability present`() {
        AndroidConnectivityObserver.isOnline(
            capabilities(internet = false, validated = false),
        ) shouldBe false
    }

    @Test
    fun `offline when capabilities are null`() {
        AndroidConnectivityObserver.isOnline(null) shouldBe false
    }
}
