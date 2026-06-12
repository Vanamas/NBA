package cz.vanama.courtflow.core.common.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNetworkCapabilities

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AndroidConnectivityObserverTest {
    private val context = RuntimeEnvironment.getApplication()
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Test
    fun `emits true when the active network has internet capability`() =
        runTest {
            val capabilities = ShadowNetworkCapabilities.newInstance()
            shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            shadowOf(connectivityManager)
                .setNetworkCapabilities(connectivityManager.activeNetwork, capabilities)

            AndroidConnectivityObserver(context).isOnline.test {
                awaitItem() shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits false when there is no active network`() =
        runTest {
            shadowOf(connectivityManager).setActiveNetworkInfo(null)

            AndroidConnectivityObserver(context).isOnline.test {
                awaitItem() shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }
}
