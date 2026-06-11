package cz.vanama.courtflow.domain.di

import cz.vanama.courtflow.domain.usecase.GetPlayerDetailUseCase
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamDetailUseCase
import org.koin.dsl.module

/** Koin module providing domain use cases. */
val domainModule =
    module {
        factory { GetPlayersUseCase(get()) }
        factory { GetPlayerDetailUseCase(get()) }
        factory { GetTeamDetailUseCase(get()) }
    }
