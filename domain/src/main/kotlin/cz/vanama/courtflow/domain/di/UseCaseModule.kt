package cz.vanama.courtflow.domain.di

import cz.vanama.courtflow.domain.usecase.GetPlayerDetailUseCase
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamDetailUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamGamesUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamPlayersUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Koin module providing domain use cases. */
val domainModule =
    module {
        factoryOf(::GetPlayersUseCase)
        factoryOf(::GetPlayerDetailUseCase)
        factoryOf(::GetTeamDetailUseCase)
        factoryOf(::GetTeamGamesUseCase)
        factoryOf(::GetTeamPlayersUseCase)
        factoryOf(::GetTeamsUseCase)
    }
