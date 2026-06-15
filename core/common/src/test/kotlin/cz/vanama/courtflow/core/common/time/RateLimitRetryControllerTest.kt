package cz.vanama.courtflow.core.common.time

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RateLimitRetryControllerTest {
    @Test
    fun `schedule emits one tick per second down to one then fires onElapsed at zero`() =
        runTest {
            val controller = RateLimitRetryController()
            val ticks = mutableListOf<Int>()
            var elapsed = 0

            controller.schedule(this, onTick = { ticks += it }, onElapsed = { elapsed++ })
            advanceUntilIdle()

            // 15..1 surfaced as ticks; the terminal 0 is delivered via onElapsed, not onTick.
            ticks shouldBe (15 downTo 1).toList()
            elapsed shouldBe 1
        }

    @Test
    fun `onElapsed fires only after the full retry window`() =
        runTest {
            val controller = RateLimitRetryController()
            var elapsed = 0

            controller.schedule(this, onTick = {}, onElapsed = { elapsed++ })

            advanceTimeBy(14_000)
            runCurrent()
            elapsed shouldBe 0

            advanceTimeBy(1_000)
            runCurrent()
            elapsed shouldBe 1
        }

    @Test
    fun `the first tick equals the retry window length`() =
        runTest {
            val controller = RateLimitRetryController()
            val ticks = mutableListOf<Int>()

            controller.schedule(this, onTick = { ticks += it }, onElapsed = {})
            runCurrent()

            ticks.first() shouldBe RateLimitRetryController.RATE_LIMIT_RETRY_SECONDS
            RateLimitRetryController.RATE_LIMIT_RETRY_SECONDS shouldBe 15
        }

    @Test
    fun `cancel stops the countdown so onElapsed never fires`() =
        runTest {
            val controller = RateLimitRetryController()
            var elapsed = 0

            controller.schedule(this, onTick = {}, onElapsed = { elapsed++ })
            advanceTimeBy(5_000)
            runCurrent()

            controller.cancel()
            advanceTimeBy(60_000)
            runCurrent()

            elapsed shouldBe 0
        }

    @Test
    fun `rescheduling cancels the previous countdown before starting a new one`() =
        runTest {
            val controller = RateLimitRetryController()
            var elapsed = 0

            controller.schedule(this, onTick = {}, onElapsed = { elapsed++ })
            advanceTimeBy(10_000)
            runCurrent()

            // Restart mid-flight (mirrors a fresh load while a retry is pending).
            controller.schedule(this, onTick = {}, onElapsed = { elapsed++ })
            advanceTimeBy(10_000)
            runCurrent()
            // Only 10s into the second window — neither the cancelled first nor the
            // second has elapsed yet.
            elapsed shouldBe 0

            advanceTimeBy(5_000)
            runCurrent()
            elapsed shouldBe 1
        }
}
