package cz.vanama.courtflow.core.common.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * [ConnectivityObserver] backed by [ConnectivityManager]'s default network
 * callback. Emits the current state immediately on collection so collectors
 * never wait for the first connectivity change.
 *
 * "Online" means a network that has *validated* internet access
 * (`NET_CAPABILITY_INTERNET` **and** `NET_CAPABILITY_VALIDATED`), so a
 * captive-portal / "connected, no internet" network reads as offline. Every
 * callback recomputes the aggregate state from [ConnectivityManager] rather
 * than emitting a hardcoded value, so a Wi-Fi↔cellular handover (the old
 * network's `onLost` arriving before the new one's `onAvailable`) does not
 * flash a spurious `false`.
 */
class AndroidConnectivityObserver(
    context: Context,
) : ConnectivityObserver {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val isOnline: Flow<Boolean> =
        callbackFlow {
            val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        trySend(true)
                    }

                    override fun onLost(network: Network) {
                        trySend(false)
                    }
                }
            trySend(aggregateOnline())
            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
        }.distinctUntilChanged()

    /**
     * True when **any** currently known network has validated internet. Reading
     * all networks (not just `activeNetwork`) absorbs the brief overlap during a
     * handover where the old network is still present and the new one already is.
     */
    private fun aggregateOnline(): Boolean =
        connectivityManager.allNetworks.any { network ->
            isOnline(connectivityManager.getNetworkCapabilities(network))
        }

    companion object {
        /**
         * Pure predicate: a network counts as online only when it advertises
         * **both** internet capability and a validated connection. Kept internal
         * and side-effect-free so it is directly unit-testable with a mocked
         * [NetworkCapabilities].
         */
        @JvmStatic
        internal fun isOnline(capabilities: NetworkCapabilities?): Boolean =
            capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
