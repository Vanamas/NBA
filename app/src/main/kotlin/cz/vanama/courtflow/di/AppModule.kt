package cz.vanama.courtflow.di

import cz.vanama.courtflow.about.DefaultAppInfoProvider
import cz.vanama.courtflow.feature.settings.AppInfoProvider
import cz.vanama.courtflow.feature.settings.AppLocaleController
import cz.vanama.courtflow.locale.DefaultAppLocaleController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * App-level Koin bindings for cross-cutting collaborators that need app-module
 * resources — the [AppLocaleController] (reads declared locale config to drive
 * the settings language picker) and the [AppInfoProvider] (reads `BuildConfig`
 * for the version name and launches the OSS-licenses screen).
 */
val appModule =
    module {
        single<AppLocaleController> { DefaultAppLocaleController(androidContext()) }
        single<AppInfoProvider> { DefaultAppInfoProvider(androidContext()) }
    }
