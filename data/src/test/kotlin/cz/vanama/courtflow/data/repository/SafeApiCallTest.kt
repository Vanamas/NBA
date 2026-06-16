package cz.vanama.courtflow.data.repository

import com.squareup.moshi.JsonDataException
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SafeApiCallTest {
    @Test
    fun `IOException maps to NETWORK`() = expectKind(IOException("offline"), DataErrorKind.NETWORK)

    @Test
    fun `HTTP 429 maps to RATE_LIMITED`() = expectKind(httpException(429), DataErrorKind.RATE_LIMITED)

    @Test
    fun `HTTP 404 maps to NOT_FOUND`() = expectKind(httpException(404), DataErrorKind.NOT_FOUND)

    @Test
    fun `HTTP 500 maps to SERVER`() = expectKind(httpException(500), DataErrorKind.SERVER)

    @Test
    fun `HTTP 400 maps to UNKNOWN`() = expectKind(httpException(400), DataErrorKind.UNKNOWN)

    @Test
    fun `IllegalArgumentException maps to UNKNOWN`() =
        expectKind(IllegalArgumentException("Player is missing an id"), DataErrorKind.UNKNOWN)

    @Test
    fun `JsonDataException maps to UNKNOWN`() =
        expectKind(JsonDataException("Expected one of [East, West] but was Intl"), DataErrorKind.UNKNOWN)

    @Test
    fun `CancellationException propagates untouched`() {
        assertThrows(CancellationException::class.java) {
            runBlocking { safeApiCall<Unit> { throw CancellationException("cancelled") } }
        }
    }

    @Test
    fun `HTTP 429 carries the x-ratelimit-reset epoch`() {
        val thrown = httpException(429, Headers.headersOf("x-ratelimit-reset", "1781550430"))
        val e =
            assertThrows(DataException::class.java) {
                runBlocking { safeApiCall<Unit> { throw thrown } }
            }
        assertEquals(DataErrorKind.RATE_LIMITED, e.kind)
        assertEquals(1781550430L, e.rateLimitResetEpochSeconds)
    }

    @Test
    fun `HTTP 429 without the reset header has a null epoch`() {
        val e =
            assertThrows(DataException::class.java) {
                runBlocking { safeApiCall<Unit> { throw httpException(429) } }
            }
        assertNull(e.rateLimitResetEpochSeconds)
    }

    @Test
    fun `HTTP 500 with a reset header still has a null epoch`() {
        val thrown = httpException(500, Headers.headersOf("x-ratelimit-reset", "1781550430"))
        val e =
            assertThrows(DataException::class.java) {
                runBlocking { safeApiCall<Unit> { throw thrown } }
            }
        assertNull(e.rateLimitResetEpochSeconds)
    }

    @Test
    fun `HTTP 429 with a non-numeric reset header has a null epoch`() {
        val thrown = httpException(429, Headers.headersOf("x-ratelimit-reset", "not-a-number"))
        val e =
            assertThrows(DataException::class.java) {
                runBlocking { safeApiCall<Unit> { throw thrown } }
            }
        assertNull(e.rateLimitResetEpochSeconds)
    }

    private fun expectKind(
        thrown: Exception,
        expected: DataErrorKind,
    ) {
        val e =
            assertThrows(DataException::class.java) {
                runBlocking { safeApiCall<Unit> { throw thrown } }
            }
        assertEquals(expected, e.kind)
    }

    private fun httpException(
        code: Int,
        headers: Headers = Headers.headersOf(),
    ): HttpException {
        val raw =
            okhttp3.Response
                .Builder()
                .code(code)
                .message("error")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("https://api.balldontlie.io/").build())
                .headers(headers)
                .build()
        return HttpException(Response.error<Any>("".toResponseBody(), raw))
    }
}
