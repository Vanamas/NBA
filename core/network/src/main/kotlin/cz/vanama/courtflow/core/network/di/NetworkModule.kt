package cz.vanama.courtflow.core.network.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import cz.vanama.courtflow.core.network.BuildConfig
import cz.vanama.courtflow.core.network.api.BallDontLieApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

private const val HTTP_LOG_TAG = "OkHttp"

/**
 * Koin module wiring the network stack: Moshi, OkHttp (with logging routed
 * to Timber and the Authorization header taken from
 * `BuildConfig.BALLDONTLIE_API_KEY`), Retrofit and the [BallDontLieApi]
 * service.
 */
val coreNetworkModule =
    module {
        single {
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        }

        single {
            HttpLoggingInterceptor { message ->
                Timber.tag(HTTP_LOG_TAG).d(message)
            }.apply {
                level =
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                redactHeader("Authorization")
            }
        }

        single {
            OkHttpClient
                .Builder()
                .addInterceptor(get<HttpLoggingInterceptor>())
                .addInterceptor { chain ->
                    val request =
                        chain
                            .request()
                            .newBuilder()
                            .addHeader("Authorization", BuildConfig.BALLDONTLIE_API_KEY)
                            .build()
                    chain.proceed(request)
                }.build()
        }

        single {
            Retrofit
                .Builder()
                .baseUrl("https://api.balldontlie.io/v1/")
                .client(get())
                .addConverterFactory(MoshiConverterFactory.create(get()))
                .build()
        }

        single {
            get<Retrofit>().create(BallDontLieApi::class.java)
        }
    }
