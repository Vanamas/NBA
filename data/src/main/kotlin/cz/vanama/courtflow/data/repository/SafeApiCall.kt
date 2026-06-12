package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.domain.error.DataException
import retrofit2.HttpException
import java.io.IOException

/**
 * Executes [block] and translates transport-specific failures (network I/O,
 * HTTP error responses) into a domain [DataException] so layers above `data`
 * never see Retrofit/OkHttp types. Anything else (including
 * [kotlinx.coroutines.CancellationException]) propagates untouched.
 */
internal suspend fun <T> safeApiCall(block: suspend () -> T): T =
    try {
        block()
    } catch (e: IOException) {
        throw DataException("Network error", e)
    } catch (e: HttpException) {
        throw DataException("API error: HTTP ${e.code()}", e)
    }
