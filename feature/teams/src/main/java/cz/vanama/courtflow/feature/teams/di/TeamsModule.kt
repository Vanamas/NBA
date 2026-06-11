package cz.vanama.courtflow.feature.teams.di

import cz.vanama.courtflow.feature.teams.detail.TeamDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Koin module providing the ViewModels of the teams feature. */
val teamsFeatureModule =
    module {
        viewModel { TeamDetailViewModel(get()) }
    }
