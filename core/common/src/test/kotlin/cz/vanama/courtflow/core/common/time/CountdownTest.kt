package cz.vanama.courtflow.core.common.time

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CountdownTest {
    @Test
    fun `counts down from the given number to zero, one tick per second`() =
        runTest {
            countdownSeconds(3).test {
                awaitItem() shouldBe 3
                awaitItem() shouldBe 2
                awaitItem() shouldBe 1
                awaitItem() shouldBe 0
                awaitComplete()
            }
        }

    @Test
    fun `zero emits a single zero and completes`() =
        runTest {
            countdownSeconds(0).test {
                awaitItem() shouldBe 0
                awaitComplete()
            }
        }

    @Test
    fun `negative input is rejected`() =
        runTest {
            countdownSeconds(-1).test {
                awaitError().shouldBeInstanceOf<IllegalArgumentException>()
            }
        }
}
