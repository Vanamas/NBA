package cz.vanama.courtflow.di

import android.content.Context
import cz.vanama.courtflow.core.common.di.coreCommonModule
import cz.vanama.courtflow.core.network.di.coreNetworkModule
import cz.vanama.courtflow.data.di.dataModule
import cz.vanama.courtflow.domain.di.domainModule
import cz.vanama.courtflow.feature.players.di.playersFeatureModule
import cz.vanama.courtflow.feature.settings.di.settingsFeatureModule
import cz.vanama.courtflow.feature.teams.di.teamsFeatureModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Verifies that the full Koin graph resolves, so a missing or mistyped
 * binding fails in `./gradlew test` instead of crashing at runtime when
 * the affected screen is opened.
 */
@OptIn(KoinExperimentalAPI::class)
class KoinModulesTest {
    @Test
    fun `koin module graph resolves`() {
        module {
            includes(
                appModule,
                coreCommonModule,
                coreNetworkModule,
                dataModule,
                domainModule,
                playersFeatureModule,
                teamsFeatureModule,
                settingsFeatureModule,
            )
        }.verify(
            // Types the constructor DSL exposes to verify() but that aren't graph
            // definitions: Int — detail ViewModels take the player/team id as a
            // runtime parameter (parametersOf); Function0 — GameRepositoryImpl's
            // defaulted clock lambda; Context — supplied by androidContext() at
            // startup and injected into AndroidConnectivityObserver and
            // DefaultAppLocaleController.
            extraTypes = listOf(Int::class, Function0::class, Context::class),
        )
    }
}
