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
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AndroidConnectivityObserverTest {
    private val context = RuntimeEnvironment.getApplication()
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun validatedCapabilities(): NetworkCapabilities {
        val capabilities = ShadowNetworkCapabilities.newInstance()
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return capabilities
    }

    private fun internetOnlyCapabilities(): NetworkCapabilities {
        val capabilities = ShadowNetworkCapabilities.newInstance()
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        return capabilities
    }

    @Test
    fun `emits true when the active network has validated internet`() =
        runTest {
            shadowOf(connectivityManager)
                .setNetworkCapabilities(connectivityManager.activeNetwork, validatedCapabilities())

            AndroidConnectivityObserver(context).isOnline.test {
                awaitItem() shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits false for a captive-portal network without validation`() =
        runTest {
            shadowOf(connectivityManager)
                .setNetworkCapabilities(connectivityManager.activeNetwork, internetOnlyCapabilities())

            AndroidConnectivityObserver(context).isOnline.test {
                awaitItem() shouldBe false
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

    @Test
    fun `losing the only network emits false`() =
        runTest {
            shadowOf(connectivityManager)
                .setNetworkCapabilities(connectivityManager.activeNetwork, validatedCapabilities())

            AndroidConnectivityObserver(context).isOnline.test {
                awaitItem() shouldBe true

                val callback = shadowOf(connectivityManager).networkCallbacks.single()
                // Drop the only network, then notify: aggregate now sees nothing.
                shadowOf(connectivityManager).setNetworkCapabilities(connectivityManager.activeNetwork, null)
                shadowOf(connectivityManager).setActiveNetworkInfo(null)
                callback.onLost(ShadowNetwork.newInstance(1))

                awaitItem() shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `wifi to cellular handover never flashes offline`() =
        runTest {
            val wifi = ShadowNetwork.newInstance(1)
            val cellular = ShadowNetwork.newInstance(2)
            // Start with Wi-Fi validated and active.
            shadowOf(connectivityManager).setNetworkCapabilities(wifi, validatedCapabilities())
            shadowOf(connectivityManager).setNetworkCapabilities(
                connectivityManager.activeNetwork,
                validatedCapabilities(),
            )

            AndroidConnectivityObserver(context).isOnline.test {
                awaitItem() shouldBe true

                val callback = shadowOf(connectivityManager).networkCallbacks.single()
                // New network already validated and present BEFORE the old one is lost.
                shadowOf(connectivityManager).setNetworkCapabilities(cellular, validatedCapabilities())
                callback.onAvailable(cellular)
                // Old network goes away; cellular is still validated, so aggregate stays true.
                shadowOf(connectivityManager).setNetworkCapabilities(wifi, null)
                callback.onLost(wifi)

                // distinctUntilChanged collapses the repeated trues; we must NOT see a false.
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `cancelling collection unregisters the callback`() =
        runTest {
            AndroidConnectivityObserver(context).isOnline.test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            shadowOf(connectivityManager).networkCallbacks shouldBe emptySet()
        }
}
