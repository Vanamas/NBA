package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

/** Observes the ids of all favorites of a given type, newest first. */
class ObserveFavoritesUseCase(
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(type: FavoriteType): Flow<List<Int>> = favoritesRepository.observeFavorites(type)
}
