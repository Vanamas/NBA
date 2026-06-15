package cz.vanama.courtflow.di

import cz.vanama.courtflow.feature.settings.AppLocaleController
import cz.vanama.courtflow.locale.DefaultAppLocaleController
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * App-level Koin bindings for cross-cutting collaborators that need app-module
 * resources — currently the [AppLocaleController], which reads the declared
 * locale config to drive the settings language picker.
 */
val appModule =
    module {
        singleOf(::DefaultAppLocaleController) { bind<AppLocaleController>() }
    }
