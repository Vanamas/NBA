package cz.vanama.courtflow.data.di

import cz.vanama.courtflow.data.repository.PlayerRepositoryImpl
import cz.vanama.courtflow.data.repository.TeamRepositoryImpl
import cz.vanama.courtflow.domain.repository.PlayerRepository
import cz.vanama.courtflow.domain.repository.TeamRepository
import org.koin.dsl.module

/** Koin module binding repository implementations to their domain interfaces. */
val dataModule =
    module {
        single<PlayerRepository> { PlayerRepositoryImpl(get()) }
        single<TeamRepository> { TeamRepositoryImpl(get()) }
    }
