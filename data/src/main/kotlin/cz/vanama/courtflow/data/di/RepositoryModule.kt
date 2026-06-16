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
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
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
        // Kept as a manual definition: GameRepositoryImpl's second constructor
        // parameter is a defaulted clock lambda (`() -> Long`) that is not in the
        // graph, and the constructor DSL would try to resolve it and fail.
        single<GameRepository> { GameRepositoryImpl(get()) }
        singleOf(::PlayerRepositoryImpl) { bind<PlayerRepository>() }
        // Manual for the same reason as GameRepository: defaulted `nowMillis` clock.
        single<TeamRepository> { TeamRepositoryImpl(get(), get(), get()) }
    }
