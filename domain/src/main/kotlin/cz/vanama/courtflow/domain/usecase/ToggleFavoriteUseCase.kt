package cz.vanama.courtflow.domain.usecase

import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.repository.FavoritesRepository

/** Toggles the favorite state of an entity: adds it when absent, removes it when present. */
class ToggleFavoriteUseCase(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(
        id: Int,
        type: FavoriteType,
    ) = favoritesRepository.toggle(id, type)
}
