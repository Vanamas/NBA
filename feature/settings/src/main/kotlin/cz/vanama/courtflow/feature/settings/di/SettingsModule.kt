package cz.vanama.courtflow.feature.settings.di

import cz.vanama.courtflow.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Koin module providing the settings feature ViewModel. */
val settingsFeatureModule =
    module {
        viewModel { SettingsViewModel(get(), get(), get()) }
    }
