package cz.vanama.courtflow.core.common.time

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Single owner of the "rate-limited, retrying in N seconds" countdown shared by
 * the detail and list ViewModels. Holds one in-flight [Job]; [schedule] cancels
 * any previous countdown before starting a new one, so a fresh load (or a second
 * consecutive rate-limit) restarts cleanly.
 *
 * The retry delay is derived from the API's `x-ratelimit-reset` header (an
 * absolute Unix epoch in seconds): `reset - now + 1`, clamped to
 * [[MIN_SECONDS], [MAX_SECONDS]]. When the header is absent ([resetEpochSeconds]
 * is null) it falls back to [FALLBACK_SECONDS]. Driven by [countdownSeconds] /
 * `delay`, so virtual-time tests complete instantly.
 *
 * Created per-ViewModel rather than injected: it owns mutable per-VM [Job] state
 * and needs no collaborators, so DI would add ceremony without value. [nowMillis]
 * is injectable purely so the header math is deterministic in tests.
 */
class RateLimitRetryController(
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private var job: Job? = null

    /**
     * Cancels any pending countdown, then starts a new one on [scope]: calls
     * [onTick] with each remaining second (high → 1) and finally [onElapsed].
     */
    fun schedule(
        resetEpochSeconds: Long?,
        scope: CoroutineScope,
        onTick: (Int) -> Unit,
        onElapsed: () -> Unit,
    ) {
        job?.cancel()
        job =
            scope.launch {
                countdownSeconds(delaySeconds(resetEpochSeconds)).collect { remaining ->
                    if (remaining > 0) onTick(remaining) else onElapsed()
                }
            }
    }

    /** Stops a pending countdown (e.g. when a manual retry pre-empts the auto-retry). */
    fun cancel() {
        job?.cancel()
        job = null
    }

    private fun delaySeconds(resetEpochSeconds: Long?): Int {
        resetEpochSeconds ?: return FALLBACK_SECONDS
        val nowSeconds = nowMillis() / MILLIS_PER_SECOND
        return (resetEpochSeconds - nowSeconds + 1)
            .coerceIn(MIN_SECONDS.toLong(), MAX_SECONDS.toLong())
            .toInt()
    }

    companion object {
        /** Wait used when the API sends no `x-ratelimit-reset` header. */
        const val FALLBACK_SECONDS = 15

        /** Floor so `reset == now` still shows one tick instead of an instant reload loop. */
        const val MIN_SECONDS = 1

        /** Cap so a bogus / far-future reset (or a skewed device clock) cannot freeze the UI. */
        const val MAX_SECONDS = 60

        private const val MILLIS_PER_SECOND = 1000L
    }
}
