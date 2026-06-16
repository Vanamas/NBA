package cz.vanama.courtflow.data.di

import androidx.room.Room
import cz.vanama.courtflow.data.local.CourtFlowDatabase
import cz.vanama.courtflow.data.repository.GameRepositoryImpl
import cz.vanama.courtflow.data.repository.PlayerRepositoryImpl
import cz.vanama.courtflow.data.repository.TeamRepositoryImpl
import cz.vanama.courtflow.domain.repository.GameRepository
import cz.vanama.courtflow.domain.repository.PlayerRepository
import cz.vanama.courtflow.domain.repository.TeamRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Koin module binding the Room cache and repository implementations. */
val dataModule =
    module {
        single {
            Room
                .databaseBuilder(androidContext(), CourtFlowDatabase::class.java, CourtFlowDatabase.NAME)
                // Pure cache of API data: dropping it on a schema change is
                // always safe because every row can be refetched.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
        single { get<CourtFlowDatabase>().teamDao() }
        single { get<CourtFlowDatabase>().cacheMetadataDao() }
        single { get<CourtFlowDatabase>().gameDao() }
        // Manual: the defaulted `nowMillis` clock (`() -> Long`) is not in the graph.
        single<GameRepository> { GameRepositoryImpl(get(), get(), get()) }
        // Manual for the same reason as GameRepository/TeamRepository: defaulted `nowMillis` clock.
        single<PlayerRepository> { PlayerRepositoryImpl(get(), get()) }
        // Manual for the same reason as GameRepository: defaulted `nowMillis` clock.
        single<TeamRepository> { TeamRepositoryImpl(get(), get(), get()) }
    }
