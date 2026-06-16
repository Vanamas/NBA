package cz.vanama.courtflow.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Records the Baseline Profile for `:app`. Run with
 * `./gradlew :app:generateReleaseBaselineProfile` — it drives the app through
 * [scrollPlayerList] on the managed device, captures the methods that run, and
 * writes them to `app/src/release/generated/baselineProfiles`, where the
 * `androidx.baselineprofile` consumer plugin bundles them into the APK so ART
 * AOT-compiles them at install time.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = APP_PACKAGE,
            maxIterations = MAX_ITERATIONS,
            stableIterations = STABLE_ITERATIONS,
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()
            waitForPlayerList()
            scrollPlayerList()
        }
    }

    private companion object {
        const val MAX_ITERATIONS = 15
        const val STABLE_ITERATIONS = 3
    }
}
