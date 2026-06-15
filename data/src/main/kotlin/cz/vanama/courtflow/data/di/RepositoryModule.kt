package cz.vanama.courtflow.data.di

import androidx.room.Room
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.data.repository.FavoritesRepositoryImpl
import cz.vanama.courtflow.data.repository.GameRepositoryImpl
import cz.vanama.courtflow.data.repository.PlayerRepositoryImpl
import cz.vanama.courtflow.data.repository.StandingsRepositoryImpl
import cz.vanama.courtflow.data.repository.TeamRepositoryImpl
import cz.vanama.courtflow.domain.repository.FavoritesRepository
import cz.vanama.courtflow.domain.repository.GameRepository
import cz.vanama.courtflow.domain.repository.PlayerRepository
import cz.vanama.courtflow.domain.repository.StandingsRepository
import cz.vanama.courtflow.domain.repository.TeamRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Koin module binding the Room cache and repository implementations. */
val dataModule =
    module {
        single {
            Room
                .databaseBuilder(androidContext(), CourtFlowDatabase::class.java, CourtFlowDatabase.NAME)
                // Pure cache of API data plus user re-creatable favorites:
                // dropping it on a schema change is acceptable.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
        single { get<CourtFlowDatabase>().teamDao() }
        single { get<CourtFlowDatabase>().favoriteDao() }
        single<GameRepository> { GameRepositoryImpl(get()) }
        single<StandingsRepository> { StandingsRepositoryImpl(get()) }
        single<PlayerRepository> { PlayerRepositoryImpl(get(), get()) }
        single<TeamRepository> { TeamRepositoryImpl(get(), get()) }
        single<FavoritesRepository> { FavoritesRepositoryImpl(get()) }
    }
