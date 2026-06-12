package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.error.DataException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
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

    private fun httpException(code: Int): HttpException = HttpException(Response.error<Any>(code, "".toResponseBody()))
}
