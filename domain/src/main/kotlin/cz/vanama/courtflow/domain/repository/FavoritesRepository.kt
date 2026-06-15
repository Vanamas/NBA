package cz.vanama.courtflow.domain.repository

import cz.vanama.courtflow.domain.model.FavoriteType
import kotlinx.coroutines.flow.Flow

/**
 * Persistent store of the user's favorite players and teams; implemented in
 * the data layer on top of a Room table that survives process death.
 */
interface FavoritesRepository {
    /** Emits whether the entity [id] of [type] is currently a favorite, updating on every change. */
    fun isFavorite(
        id: Int,
        type: FavoriteType,
    ): Flow<Boolean>

    /** Emits the ids of all favorites of [type], newest first; updates on every change. */
    fun observeFavorites(type: FavoriteType): Flow<List<Int>>

    /** Adds the entity [id] of [type] to favorites when absent, removes it when present. */
    suspend fun toggle(
        id: Int,
        type: FavoriteType,
    )
}
