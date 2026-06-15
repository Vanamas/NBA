package cz.vanama.courtflow.feature.players.di

import cz.vanama.courtflow.feature.players.detail.PlayerDetailViewModel
import cz.vanama.courtflow.feature.players.list.PlayerListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module providing the ViewModels of the players feature. */
val playersFeatureModule =
    module {
        viewModelOf(::PlayerListViewModel)
        // playerId is supplied at navigation time via parametersOf; the
        // constructor DSL matches it by type and resolves the rest from the graph.
        viewModelOf(::PlayerDetailViewModel)
    }
