package cz.vanama.courtflow.core.common.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DataStoreThemePreferencesRepositoryTest {
    private lateinit var file: File
    private lateinit var store: DataStore<Preferences>
    private lateinit var repository: DataStoreThemePreferencesRepository

    @Before
    fun setUp() {
        file = File.createTempFile("theme_prefs", ".preferences_pb")
        file.delete()
        store = PreferenceDataStoreFactory.create { file }
        repository = DataStoreThemePreferencesRepository(store)
    }

    @After
    fun tearDown() {
        file.delete()
    }

    @Test
    fun `emits defaults when nothing was written`() =
        runTest {
            repository.themePreferences.first() shouldBe ThemePreferences()
        }

    @Test
    fun `persists dynamic color flag`() =
        runTest {
            repository.setDynamicColor(false)
            repository.themePreferences.first().dynamicColor shouldBe false
        }

    @Test
    fun `persists theme mode`() =
        runTest {
            repository.setThemeMode(ThemeMode.DARK)
            repository.themePreferences.first().themeMode shouldBe ThemeMode.DARK
        }

    @Test
    fun `persists true-black flag`() =
        runTest {
            repository.setTrueBlack(true)
            repository.themePreferences.first().trueBlack shouldBe true
        }

    @Test
    fun `falls back to SYSTEM for an unknown stored mode`() =
        runTest {
            repository.setThemeMode(ThemeMode.LIGHT)
            repository.setThemeModeRaw("GALAXY")
            repository.themePreferences.first().themeMode shouldBe ThemeMode.SYSTEM
        }
}
