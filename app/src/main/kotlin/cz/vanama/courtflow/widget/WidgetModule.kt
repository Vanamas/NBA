package cz.vanama.courtflow.widget

import org.koin.dsl.module

/**
 * Koin bindings for the home-screen widget. [FavoriteTeamProvider] is bound to
 * [FavoritesFavoriteTeamProvider], which reads the user's first favorite team
 * (F2) and falls back to the first available team when none is set — swapping
 * the binding here is the only change needed to alter that strategy.
 */
val widgetModule =
    module {
        single<FavoriteTeamProvider> {
            FavoritesFavoriteTeamProvider(observeFavorites = get(), getTeams = get())
        }
        factory { WidgetDataLoader(favoriteTeamProvider = get(), getTeamGames = get()) }
    }
