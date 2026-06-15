package cz.vanama.courtflow.domain.di

import cz.vanama.courtflow.domain.usecase.GetPlayerDetailUseCase
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamDetailUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamGamesUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamPlayersUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamStandingUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamsUseCase
import cz.vanama.courtflow.domain.usecase.IsFavoriteUseCase
import cz.vanama.courtflow.domain.usecase.ObserveFavoritesUseCase
import cz.vanama.courtflow.domain.usecase.ToggleFavoriteUseCase
import org.koin.dsl.module

/** Koin module providing domain use cases. */
val domainModule =
    module {
        factory { GetPlayersUseCase(get()) }
        factory { GetPlayerDetailUseCase(get()) }
        factory { GetTeamDetailUseCase(get()) }
        factory { GetTeamGamesUseCase(get()) }
        factory { GetTeamStandingUseCase(get()) }
        factory { GetTeamPlayersUseCase(get()) }
        factory { GetTeamsUseCase(get()) }
        factory { IsFavoriteUseCase(get()) }
        factory { ToggleFavoriteUseCase(get()) }
        factory { ObserveFavoritesUseCase(get()) }
    }
