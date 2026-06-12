package cz.vanama.courtflow.core.common.connectivity

import kotlinx.coroutines.flow.Flow

/** Observes whether the device currently has an internet-capable network. */
interface ConnectivityObserver {
    /** Emits the current state on collection, then on every change; distinct. */
    val isOnline: Flow<Boolean>
}
