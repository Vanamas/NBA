package cz.vanama.courtflow.data.di

import cz.vanama.courtflow.domain.repository.FavoritesRepository
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.annotation.KoinInternalApi

@OptIn(KoinInternalApi::class)
class FavoritesModuleTest {
    @Test
    fun `dataModule declares a FavoritesRepository binding`() {
        val definition =
            dataModule.mappings.values.firstOrNull {
                it.beanDefinition.primaryType == FavoritesRepository::class
            }
        assertNotNull("dataModule must bind FavoritesRepository", definition)
    }
}
