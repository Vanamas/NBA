package cz.vanama.courtflow.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

/** Application id of the app under instrumentation. */
internal const val APP_PACKAGE = "cz.vanama.courtflow"

// A player card exposes the player's full name as its avatar content-description
// ("Firstname Lastname"); the loading skeleton exposes none. Waiting for one is
// how we know the list has data to scroll. The Compose LazyVerticalGrid is not
// reported as a scrollable node, so By.scrollable(true) can't be used here.
private val PLAYER_NAME: Pattern = Pattern.compile("\\p{Lu}.+\\s.+")
private const val CONTENT_WAIT_TIMEOUT_MS = 10_000L

private const val SCROLL_ITERATIONS = 4

// Swipe as a fraction of the screen: a drag from mid-screen up into the upper
// third. Verified on device to scroll the list while keeping the app
// foregrounded.
private const val SWIPE_START_PERCENT = 55
private const val SWIPE_END_PERCENT = 25
private const val PERCENT_BASE = 100
private const val SWIPE_DURATION_MS = 250

/**
 * Waits until real player cards have rendered — call before [scrollPlayerList].
 * After a cold start the app first shows a loading skeleton (no cards); swiping
 * it scrolls nothing and the benchmark records zero frames.
 */
internal fun MacrobenchmarkScope.waitForPlayerList() {
    device.wait(Until.hasObject(By.desc(PLAYER_NAME)), CONTENT_WAIT_TIMEOUT_MS)
}

/**
 * Scrolls down the player list a few times — the cold-start fast-scroll the user
 * reported janking. Shared by [BaselineProfileGenerator] (to AOT-compile these
 * paths) and [ScrollBenchmark] (to measure them).
 *
 * The swipe is injected via the shell `input swipe` command rather than
 * [androidx.test.uiautomator.UiDevice.swipe]: on a gesture-nav device the latter's
 * synthetic gesture is claimed by the system edge-swipe monitor and never reaches
 * the app, so the app renders nothing and FrameTimingMetric records zero frames.
 * `input swipe` (shell uid) delivers the drag to the app, verified on device.
 */
internal fun MacrobenchmarkScope.scrollPlayerList() {
    val centerX = device.displayWidth / 2
    val startY = device.displayHeight * SWIPE_START_PERCENT / PERCENT_BASE
    val endY = device.displayHeight * SWIPE_END_PERCENT / PERCENT_BASE
    repeat(SCROLL_ITERATIONS) {
        device.executeShellCommand("input swipe $centerX $startY $centerX $endY $SWIPE_DURATION_MS")
        device.waitForIdle()
    }
}
