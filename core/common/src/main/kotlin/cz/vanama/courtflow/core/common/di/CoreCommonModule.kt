package cz.vanama.courtflow.core.common.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import cz.vanama.courtflow.core.common.connectivity.AndroidConnectivityObserver
import cz.vanama.courtflow.core.common.connectivity.ConnectivityObserver
import cz.vanama.courtflow.core.common.settings.DataStoreThemePreferencesRepository
import cz.vanama.courtflow.core.common.settings.ThemePreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

/** Koin module providing the cross-cutting services of core:common. */
val coreCommonModule =
    module {
        single<ConnectivityObserver> { AndroidConnectivityObserver(androidContext()) }
        single<DataStore<Preferences>> { androidContext().themeDataStore }
        single<ThemePreferencesRepository> { DataStoreThemePreferencesRepository(get()) }
    }
