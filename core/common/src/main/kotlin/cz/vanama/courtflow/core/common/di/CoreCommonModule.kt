package cz.vanama.courtflow.core.common.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import cz.vanama.courtflow.core.common.connectivity.AndroidConnectivityObserver
import cz.vanama.courtflow.core.common.connectivity.ConnectivityObserver
import cz.vanama.courtflow.core.common.settings.DataStoreRecentlyViewedStore
import cz.vanama.courtflow.core.common.settings.DataStoreThemePreferencesStore
import cz.vanama.courtflow.core.common.settings.RecentlyViewedStore
import cz.vanama.courtflow.core.common.settings.ThemePreferencesStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")
private val Context.recentlyViewedDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "recently_viewed_prefs")

/** Distinguishes the two Preferences DataStores bound in this module. */
private val themeDataStoreQualifier = named("themeDataStore")
private val recentlyViewedDataStoreQualifier = named("recentlyViewedDataStore")

/** Koin module providing the cross-cutting services of core:common. */
val coreCommonModule =
    module {
        single<ConnectivityObserver> { AndroidConnectivityObserver(androidContext()) }
        single<DataStore<Preferences>>(themeDataStoreQualifier) { androidContext().themeDataStore }
        single<DataStore<Preferences>>(recentlyViewedDataStoreQualifier) {
            androidContext().recentlyViewedDataStore
        }
        single<ThemePreferencesStore> {
            DataStoreThemePreferencesStore(get(themeDataStoreQualifier))
        }
        single<RecentlyViewedStore> {
            DataStoreRecentlyViewedStore(get(recentlyViewedDataStoreQualifier))
        }
    }
