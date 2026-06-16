package cz.vanama.courtflow.core.common.time

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RateLimitRetryControllerTest {
    // now = nowSeconds * 1000 ms
    private fun controllerAtSecond(nowSeconds: Long) = RateLimitRetryController(nowMillis = { nowSeconds * 1000 })

    @Test
    fun `delay is reset minus now plus one second`() =
        runTest {
            val ticks = mutableListOf<Int>()
            var elapsed = false
            controllerAtSecond(1000).schedule(
                resetEpochSeconds = 1010L,
                scope = this,
                onTick = { ticks += it },
                onElapsed = { elapsed = true },
            )
            advanceUntilIdle()

            ticks.first() shouldBe 11 // (1010 - 1000) + 1
            ticks.last() shouldBe 1
            elapsed shouldBe true
        }

    @Test
    fun `a future reset is capped at MAX_SECONDS`() =
        runTest {
            val ticks = mutableListOf<Int>()
            controllerAtSecond(1000).schedule(
                resetEpochSeconds = 1_000_000L,
                scope = this,
                onTick = { ticks += it },
                onElapsed = {},
            )
            advanceUntilIdle()

            ticks.first() shouldBe RateLimitRetryController.MAX_SECONDS
        }

    @Test
    fun `a reset in the past is clamped to MIN_SECONDS`() =
        runTest {
            val ticks = mutableListOf<Int>()
            controllerAtSecond(1000).schedule(
                resetEpochSeconds = 500L,
                scope = this,
                onTick = { ticks += it },
                onElapsed = {},
            )
            advanceUntilIdle()

            ticks.first() shouldBe RateLimitRetryController.MIN_SECONDS
        }

    @Test
    fun `a reset equal to now is clamped to MIN_SECONDS`() =
        runTest {
            val ticks = mutableListOf<Int>()
            controllerAtSecond(1000).schedule(
                resetEpochSeconds = 1000L,
                scope = this,
                onTick = { ticks += it },
                onElapsed = {},
            )
            advanceUntilIdle()

            ticks.first() shouldBe RateLimitRetryController.MIN_SECONDS
        }

    @Test
    fun `a null reset uses the fallback`() =
        runTest {
            val ticks = mutableListOf<Int>()
            controllerAtSecond(1000).schedule(
                resetEpochSeconds = null,
                scope = this,
                onTick = { ticks += it },
                onElapsed = {},
            )
            advanceUntilIdle()

            ticks.first() shouldBe RateLimitRetryController.FALLBACK_SECONDS
        }

    @Test
    fun `cancel stops a pending countdown`() =
        runTest {
            val ticks = mutableListOf<Int>()
            var elapsed = false
            val controller = controllerAtSecond(1000)
            controller.schedule(
                resetEpochSeconds = null,
                scope = this,
                onTick = { ticks += it },
                onElapsed = { elapsed = true },
            )
            controller.cancel()
            advanceUntilIdle()

            ticks.isEmpty() shouldBe true
            elapsed shouldBe false
        }

    @Test
    fun `scheduling again restarts the countdown and elapses once`() =
        runTest {
            var elapsedCount = 0
            val controller = controllerAtSecond(1000)
            controller.schedule(
                resetEpochSeconds = null,
                scope = this,
                onTick = {},
                onElapsed = { elapsedCount++ },
            )
            controller.schedule(
                resetEpochSeconds = 1010L,
                scope = this,
                onTick = {},
                onElapsed = { elapsedCount++ },
            )
            advanceUntilIdle()

            elapsedCount shouldBe 1
        }
}
