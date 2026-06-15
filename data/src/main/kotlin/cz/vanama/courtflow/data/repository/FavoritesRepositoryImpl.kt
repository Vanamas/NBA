package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.data.local.dao.FavoriteDao
import cz.vanama.courtflow.data.local.entity.FavoriteEntity
import cz.vanama.courtflow.domain.model.FavoriteType
import cz.vanama.courtflow.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

/**
 * [FavoritesRepository] backed by the Room `favorites` table. The
 * [FavoriteType] enum is stored by its `name`, so reads and the toggle map
 * the enum to that string before touching the DAO.
 */
class FavoritesRepositoryImpl(
    private val favoriteDao: FavoriteDao,
) : FavoritesRepository {
    override fun isFavorite(
        id: Int,
        type: FavoriteType,
    ): Flow<Boolean> = favoriteDao.isFavorite(id, type.name)

    override fun observeFavorites(type: FavoriteType): Flow<List<Int>> = favoriteDao.observeIds(type.name)

    override suspend fun toggle(
        id: Int,
        type: FavoriteType,
    ) {
        if (favoriteDao.exists(id, type.name)) {
            favoriteDao.delete(id, type.name)
        } else {
            favoriteDao.insert(FavoriteEntity(id = id, type = type.name, addedAt = System.currentTimeMillis()))
        }
    }
}
