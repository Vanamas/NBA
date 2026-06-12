package cz.vanama.courtflow.core.network.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import cz.vanama.courtflow.core.network.BuildConfig
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

private const val HTTP_LOG_TAG = "OkHttp"
private const val HEADER_AUTHORIZATION = "Authorization"

/**
 * Koin module wiring the network stack: Moshi, OkHttp (with logging routed
 * to Timber and the Authorization header taken from
 * `BuildConfig.BALLDONTLIE_API_KEY`), Retrofit and the [NBAApi] service
 * generated from the official balldontlie OpenAPI definition in
 * `openapi/nba.yml`.
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
                redactHeader(HEADER_AUTHORIZATION)
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
                            .addHeader(HEADER_AUTHORIZATION, BuildConfig.BALLDONTLIE_API_KEY)
                            .build()
                    chain.proceed(request)
                }.build()
        }

        single {
            Retrofit
                .Builder()
                .baseUrl(BuildConfig.BALLDONTLIE_BASE_URL)
                .client(get())
                .addConverterFactory(MoshiConverterFactory.create(get()))
                .build()
        }

        single {
            get<Retrofit>().create(NBAApi::class.java)
        }
    }
