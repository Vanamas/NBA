package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.designsystem.R
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Unit tests of the shared [errorMessage] mapper. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ErrorMessageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = RuntimeEnvironment.getApplication()

    private fun resolve(kind: DataErrorKind?): String {
        var result = ""
        composeTestRule.setContent { result = errorMessage(kind) }
        composeTestRule.waitForIdle()
        return result
    }

    private fun resolve(error: Throwable): String {
        var result = ""
        composeTestRule.setContent { result = errorMessage(error) }
        composeTestRule.waitForIdle()
        return result
    }

    @Test
    fun `network kind resolves to the network message`() {
        resolve(DataErrorKind.NETWORK) shouldBe context.getString(R.string.error_network)
    }

    @Test
    fun `rate limited kind resolves to the rate limit message`() {
        resolve(DataErrorKind.RATE_LIMITED) shouldBe context.getString(R.string.error_rate_limited)
    }

    @Test
    fun `not found kind resolves to the not found message`() {
        resolve(DataErrorKind.NOT_FOUND) shouldBe context.getString(R.string.error_not_found)
    }

    @Test
    fun `server kind resolves to the server message`() {
        resolve(DataErrorKind.SERVER) shouldBe context.getString(R.string.error_server)
    }

    @Test
    fun `null kind resolves to the unknown message`() {
        resolve(null) shouldBe context.getString(R.string.error_unknown)
    }

    @Test
    fun `DataException throwable resolves through its kind`() {
        resolve(DataException(DataErrorKind.SERVER)) shouldBe context.getString(R.string.error_server)
    }

    @Test
    fun `foreign throwable resolves to the unknown message`() {
        resolve(IllegalStateException("boom")) shouldBe context.getString(R.string.error_unknown)
    }
}
