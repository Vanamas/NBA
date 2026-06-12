package cz.vanama.courtflow

import android.app.Application
import cz.vanama.courtflow.core.network.di.coreNetworkModule
import cz.vanama.courtflow.data.di.dataModule
import cz.vanama.courtflow.domain.di.domainModule
import cz.vanama.courtflow.feature.players.di.playersFeatureModule
import cz.vanama.courtflow.feature.teams.di.teamsFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application entry point; starts the Koin container with all DI modules.
 */
class CourtFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@CourtFlowApplication)
            modules(
                coreNetworkModule,
                dataModule,
                domainModule,
                playersFeatureModule,
                teamsFeatureModule,
            )
        }
    }
}
