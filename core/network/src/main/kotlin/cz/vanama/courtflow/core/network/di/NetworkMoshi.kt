package cz.vanama.courtflow.core.network.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import cz.vanama.courtflow.core.network.generated.model.NBATeam

/**
 * Single source of the Moshi configuration used to (de)serialize the generated
 * balldontlie models, shared by [coreNetworkModule] and the contract tests so
 * production and tests can never drift apart.
 */
internal object NetworkMoshi {
    fun create(): Moshi =
        Moshi
            .Builder()
            // The /teams endpoint returns BAA-era teams whose conference/division
            // are not valid enum values ("    " / ""). Without a fallback Moshi's
            // built-in enum adapter throws and the whole page fails to parse; map
            // unknown values to null instead. The data layer already treats a blank
            // conference as a trailing fallback section.
            .addEnumFallback<NBATeam.Conference>()
            .addEnumFallback<NBATeam.Division>()
            .add(KotlinJsonAdapterFactory())
            .build()

    private inline fun <reified T : Enum<T>> Moshi.Builder.addEnumFallback(): Moshi.Builder =
        add(T::class.java, EnumJsonAdapter.create(T::class.java).withUnknownFallback(null))
}
