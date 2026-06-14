package cz.vanama.courtflow.core.common.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DataStoreThemePreferencesStoreTest {
    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var store: DataStoreThemePreferencesStore

    @Before
    fun setUp() {
        file = File.createTempFile("theme_prefs", ".preferences_pb")
        file.delete()
        dataStore = PreferenceDataStoreFactory.create { file }
        store = DataStoreThemePreferencesStore(dataStore)
    }

    @After
    fun tearDown() {
        file.delete()
    }

    @Test
    fun `emits defaults when nothing was written`() =
        runTest {
            store.themePreferences.first() shouldBe ThemePreferences()
        }

    @Test
    fun `persists dynamic color flag`() =
        runTest {
            store.setDynamicColor(false)
            store.themePreferences.first().dynamicColor shouldBe false
        }

    @Test
    fun `persists theme mode`() =
        runTest {
            store.setThemeMode(ThemeMode.DARK)
            store.themePreferences.first().themeMode shouldBe ThemeMode.DARK
        }

    @Test
    fun `persists true-black flag`() =
        runTest {
            store.setTrueBlack(true)
            store.themePreferences.first().trueBlack shouldBe true
        }

    @Test
    fun `falls back to SYSTEM for an unknown stored mode`() =
        runTest {
            store.setThemeMode(ThemeMode.LIGHT)
            store.setThemeModeRaw("GALAXY")
            store.themePreferences.first().themeMode shouldBe ThemeMode.SYSTEM
        }

    @Test
    fun `emits defaults when the DataStore read fails with IOException`() =
        runTest {
            val failing =
                object : DataStore<Preferences> {
                    override val data: Flow<Preferences> = flow { throw IOException("corrupt prefs") }

                    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                        throw UnsupportedOperationException()
                }
            DataStoreThemePreferencesStore(failing).themePreferences.first() shouldBe ThemePreferences()
        }
}
