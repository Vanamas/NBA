package cz.vanama.courtflow.di

import cz.vanama.courtflow.feature.players.detail.PlayerDetailViewModel
import cz.vanama.courtflow.feature.players.list.PlayerListViewModel
import cz.vanama.courtflow.feature.teams.detail.TeamDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Koin module providing all screen ViewModels. */
val viewModelModule =
    module {
        viewModel { PlayerListViewModel(get()) }
        viewModel { PlayerDetailViewModel(get()) }
        viewModel { TeamDetailViewModel(get()) }
    }
