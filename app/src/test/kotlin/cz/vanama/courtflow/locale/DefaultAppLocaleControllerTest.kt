package cz.vanama.courtflow.locale

import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DefaultAppLocaleControllerTest {
    private val controller = DefaultAppLocaleController(ApplicationProvider.getApplicationContext())

    @After
    fun tearDown() {
        // CourtFlowApplication (instantiated by Robolectric) starts Koin; stop it.
        stopKoin()
    }

    @Test
    fun `supported tags come from the declared locales_config`() {
        assertEquals(listOf("en", "cs"), controller.supportedLanguageTags())
    }

    @Test
    fun `current tag is empty when following the system`() {
        assertEquals("", controller.currentLanguageTag())
    }
}
