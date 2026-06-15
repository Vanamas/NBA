package cz.vanama.courtflow.feature.players.di

import cz.vanama.courtflow.feature.players.detail.PlayerDetailViewModel
import cz.vanama.courtflow.feature.players.list.PlayerListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Koin module providing the ViewModels of the players feature. */
val playersFeatureModule =
    module {
        viewModel { PlayerListViewModel(get(), get(), get(), get()) }
        viewModel { (playerId: Int) -> PlayerDetailViewModel(playerId, get(), get(), get(), get()) }
    }
