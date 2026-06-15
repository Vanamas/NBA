package cz.vanama.courtflow.widget

import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.usecase.GetTeamsUseCase
import cz.vanama.courtflow.domain.usecase.ObserveFavoritesUseCase
import kotlinx.coroutines.flow.first

/**
 * Supplies the team id the widget should display.
 *
 * This is the seam between the widget and the source of "which team to show".
 * Keeping it a one-method interface keeps [WidgetDataLoader] trivially testable
 * (its tests inject a lambda) and lets the resolution strategy change without
 * touching the loader.
 */
fun interface FavoriteTeamProvider {
    /** The favorite team id, or `null` when none is configured and none can be derived. */
    suspend fun favoriteTeamId(): Int?
}

/**
 * Resolves the team the widget shows from the user's favorites (F2): it returns
 * the id of the first favorite of type [FavoriteType.TEAM]. When the user has
 * no favorite team yet, it falls back deterministically to the id of the first
 * team returned by [GetTeamsUseCase], so the widget still has something to show.
 * Returns `null` only when neither a favorite nor any team can be resolved.
 */
class FavoritesFavoriteTeamProvider(
    private val observeFavorites: ObserveFavoritesUseCase,
    private val getTeams: GetTeamsUseCase,
) : FavoriteTeamProvider {
    override suspend fun favoriteTeamId(): Int? = firstFavoriteTeamId() ?: firstAvailableTeamId()

    private suspend fun firstFavoriteTeamId(): Int? =
        runCatching { observeFavorites(FavoriteType.TEAM).first().firstOrNull() }.getOrNull()

    private suspend fun firstAvailableTeamId(): Int? = runCatching { getTeams().first().firstOrNull()?.id }.getOrNull()
}
