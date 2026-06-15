package cz.vanama.courtflow.feature.settings.di

import cz.vanama.courtflow.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module providing the settings feature ViewModel. */
val settingsFeatureModule =
    module {
        viewModelOf(::SettingsViewModel)
    }
