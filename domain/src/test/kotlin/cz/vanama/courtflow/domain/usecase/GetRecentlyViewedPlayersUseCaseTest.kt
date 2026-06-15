package cz.vanama.courtflow.domain.usecase

import app.cash.turbine.test
import cz.vanama.courtflow.core.common.settings.RecentlyViewedStore
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.repository.PlayerRepository
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetRecentlyViewedPlayersUseCaseTest {
    private val team = Team(1, "LAL", "Los Angeles", "West", "Pacific", "Los Angeles Lakers", "Lakers")

    private fun player(id: Int) = Player(id = id, firstName = "P$id", lastName = "L$id", position = "G", team = team)

    private val store = mockk<RecentlyViewedStore>()
    private val repository = mockk<PlayerRepository>()
    private val useCase get() = GetRecentlyViewedPlayersUseCase(store, repository)

    @Test
    fun `resolves ids to players preserving order`() =
        runTest {
            every { store.recentlyViewedIds } returns flowOf(listOf(3, 1))
            coEvery { repository.getPlayerById(3) } returns player(3)
            coEvery { repository.getPlayerById(1) } returns player(1)

            useCase().test {
                awaitItem() shouldBe listOf(player(3), player(1))
                awaitComplete()
            }
        }

    @Test
    fun `emits empty list when no ids are stored`() =
        runTest {
            every { store.recentlyViewedIds } returns flowOf(emptyList())

            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }

    @Test
    fun `skips ids that fail to resolve`() =
        runTest {
            every { store.recentlyViewedIds } returns flowOf(listOf(3, 2, 1))
            coEvery { repository.getPlayerById(3) } returns player(3)
            coEvery { repository.getPlayerById(2) } throws RuntimeException("boom")
            coEvery { repository.getPlayerById(1) } returns player(1)

            useCase().test {
                awaitItem() shouldBe listOf(player(3), player(1))
                awaitComplete()
            }
        }

    @Test
    fun `re-emits when the stored ids change`() =
        runTest {
            val ids = MutableStateFlow(listOf(1))
            every { store.recentlyViewedIds } returns ids
            coEvery { repository.getPlayerById(1) } returns player(1)
            coEvery { repository.getPlayerById(2) } returns player(2)

            useCase().test {
                awaitItem() shouldBe listOf(player(1))
                ids.value = listOf(2, 1)
                awaitItem() shouldBe listOf(player(2), player(1))
            }
        }
}
