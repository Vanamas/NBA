package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

/** Observes whether a single entity is currently a favorite. */
class IsFavoriteUseCase(
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(
        id: Int,
        type: FavoriteType,
    ): Flow<Boolean> = favoritesRepository.isFavorite(id, type)
}
