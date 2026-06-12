package cz.vanama.courtflow.feature.teams.di

import cz.vanama.courtflow.feature.teams.detail.TeamDetailViewModel
import cz.vanama.courtflow.feature.teams.list.TeamListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Koin module providing the ViewModels of the teams feature. */
val teamsFeatureModule =
    module {
        viewModel { (teamId: Int) -> TeamDetailViewModel(teamId, get()) }
        viewModel { TeamListViewModel(get()) }
    }
