package cz.vanama.courtflow.data.repository

import com.squareup.moshi.JsonDataException
import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.error.DataException
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection

/**
 * Executes [block] and translates data-layer failures — network I/O, HTTP
 * error responses, Moshi deserialization ([JsonDataException]) and mapper
 * invariant violations ([IllegalArgumentException] from `requireNotNull`) —
 * into a domain [DataException] so layers above `data` never see transport
 * or parsing types. [kotlinx.coroutines.CancellationException] propagates
 * untouched: it extends [IllegalStateException], so no caught type can
 * match it.
 */
internal suspend fun <T> safeApiCall(block: suspend () -> T): T =
    try {
        block()
    } catch (e: IOException) {
        e.failAsDataException()
    } catch (e: HttpException) {
        e.failAsDataException()
    } catch (e: JsonDataException) {
        e.failAsDataException()
    } catch (e: IllegalArgumentException) {
        e.failAsDataException()
    }

/** Throws the receiver translated into a classified [DataException]. */
private fun Exception.failAsDataException(): Nothing = throw toDataException()

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
