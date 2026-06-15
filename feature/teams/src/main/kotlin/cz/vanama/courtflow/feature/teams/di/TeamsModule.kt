package cz.vanama.courtflow.feature.teams.di

import cz.vanama.courtflow.feature.teams.detail.TeamDetailViewModel
import cz.vanama.courtflow.feature.teams.list.TeamListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module providing the ViewModels of the teams feature. */
val teamsFeatureModule =
    module {
        // teamId is supplied at navigation time via parametersOf; the
        // constructor DSL matches it by type and resolves the rest from the graph.
        viewModelOf(::TeamDetailViewModel)
        viewModelOf(::TeamListViewModel)
    }
