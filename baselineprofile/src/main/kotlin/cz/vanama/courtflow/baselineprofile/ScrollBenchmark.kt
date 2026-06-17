package cz.vanama.courtflow.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Quantifies the cold-start scroll jank the user reported by measuring frame
 * timing of [scrollPlayerList] under two compilation modes. Run with
 * `./gradlew :baselineprofile:connectedBenchmarkAndroidTest` (or the managed
 * device variant) and compare `frameOverrunMs` / `frameDurationCpuMs`:
 *
 * - [scrollCompilationNone] — no AOT compilation, i.e. the worst case that
 *   most resembles a fresh cold start. This is the "failing test" that proves
 *   the jank is a compilation problem.
 * - [scrollCompilationBaselineProfile] — the generated profile applied; the
 *   percentiles should drop materially, confirming the fix.
 */
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollCompilationNone() = scroll(CompilationMode.None())

    @Test
    fun scrollCompilationBaselineProfile() = scroll(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun scroll(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = APP_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = ITERATIONS,
            setupBlock = { pressHome() },
        ) {
            // StartupMode.COLD kills the app after setupBlock, so the launch must
            // happen here in the measured block — otherwise the app is dead during
            // measurement and the swipe lands on the launcher (zero app frames).
            startActivityAndWait()
            waitForPlayerList()
            scrollPlayerList()
        }
    }

    private companion object {
        const val ITERATIONS = 10
    }
}
