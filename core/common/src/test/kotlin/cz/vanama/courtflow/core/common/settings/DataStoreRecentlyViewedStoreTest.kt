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
class DataStoreRecentlyViewedStoreTest {
    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var store: DataStoreRecentlyViewedStore

    @Before
    fun setUp() {
        file = File.createTempFile("recently_viewed", ".preferences_pb")
        file.delete()
        dataStore = PreferenceDataStoreFactory.create { file }
        store = DataStoreRecentlyViewedStore(dataStore)
    }

    @After
    fun tearDown() {
        file.delete()
    }

    @Test
    fun `emits empty list when nothing was recorded`() =
        runTest {
            store.recentlyViewedIds.first() shouldBe emptyList()
        }

    @Test
    fun `records a single view`() =
        runTest {
            store.recordView(7)
            store.recentlyViewedIds.first() shouldBe listOf(7)
        }

    @Test
    fun `newest recorded id comes first`() =
        runTest {
            store.recordView(1)
            store.recordView(2)
            store.recordView(3)
            store.recentlyViewedIds.first() shouldBe listOf(3, 2, 1)
        }

    @Test
    fun `re-recording an id moves it to the front without duplicating`() =
        runTest {
            store.recordView(1)
            store.recordView(2)
            store.recordView(1)
            store.recentlyViewedIds.first() shouldBe listOf(1, 2)
        }

    @Test
    fun `history is capped at MAX_RECENTLY_VIEWED keeping the newest`() =
        runTest {
            (1..RecentlyViewedStore.MAX_RECENTLY_VIEWED + 3).forEach { store.recordView(it) }

            val ids = store.recentlyViewedIds.first()
            ids.size shouldBe RecentlyViewedStore.MAX_RECENTLY_VIEWED
            ids.first() shouldBe RecentlyViewedStore.MAX_RECENTLY_VIEWED + 3
            ids.contains(1) shouldBe false
        }

    @Test
    fun `emits empty list when the DataStore read fails with IOException`() =
        runTest {
            val failing =
                object : DataStore<Preferences> {
                    override val data: Flow<Preferences> = flow { throw IOException("corrupt prefs") }

                    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                        throw UnsupportedOperationException()
                }
            DataStoreRecentlyViewedStore(failing).recentlyViewedIds.first() shouldBe emptyList()
        }
}
