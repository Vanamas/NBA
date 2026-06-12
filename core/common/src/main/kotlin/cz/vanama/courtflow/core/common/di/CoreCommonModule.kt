package cz.vanama.courtflow.core.common.di

import cz.vanama.courtflow.core.common.connectivity.AndroidConnectivityObserver
import cz.vanama.courtflow.core.common.connectivity.ConnectivityObserver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Koin module providing the cross-cutting services of core:common. */
val coreCommonModule =
    module {
        single<ConnectivityObserver> { AndroidConnectivityObserver(androidContext()) }
    }
