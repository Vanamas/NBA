package cz.vanama.courtflow.about

import androidx.test.core.app.ApplicationProvider
import cz.vanama.courtflow.BuildConfig
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DefaultAppInfoProviderTest {
    private val provider = DefaultAppInfoProvider(ApplicationProvider.getApplicationContext())

    @After
    fun tearDown() {
        // CourtFlowApplication (instantiated by Robolectric) starts Koin; stop it.
        stopKoin()
    }

    @Test
    fun `version name comes from BuildConfig`() {
        assertEquals(BuildConfig.VERSION_NAME, provider.versionName)
    }
}
