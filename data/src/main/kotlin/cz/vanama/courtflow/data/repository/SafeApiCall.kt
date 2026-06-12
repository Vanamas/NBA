package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.error.DataException
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection

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
        throw e.toDataException()
    } catch (e: HttpException) {
        throw e.toDataException()
    }

/** Classifies a transport failure into a domain [DataException]. */
internal fun Exception.toDataException(): DataException =
    when (this) {
        is HttpException ->
            DataException(
                kind =
                    when {
                        code() == HTTP_TOO_MANY_REQUESTS -> DataErrorKind.RATE_LIMITED
                        code() == HttpURLConnection.HTTP_NOT_FOUND -> DataErrorKind.NOT_FOUND
                        code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> DataErrorKind.SERVER
                        else -> DataErrorKind.UNKNOWN
                    },
                message = "HTTP ${code()}",
                cause = this,
            )
        is IOException -> DataException(DataErrorKind.NETWORK, "Network error", this)
        else -> DataException(DataErrorKind.UNKNOWN, message, this)
    }

/** Not among [HttpURLConnection]'s status-code constants. */
private const val HTTP_TOO_MANY_REQUESTS = 429
