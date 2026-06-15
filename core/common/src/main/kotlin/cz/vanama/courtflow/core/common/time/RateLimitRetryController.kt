package cz.vanama.courtflow.core.common.time

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Drives the "rate-limited, retrying in N seconds" countdown shared by the
 * detail and list ViewModels. Holds a single in-flight [Job]; [schedule]
 * cancels any previous countdown before starting a new one, so a fresh load
 * (or a second consecutive rate-limit) restarts cleanly.
 *
 * Each tick of [countdownSeconds] from [RATE_LIMIT_RETRY_SECONDS] down to `1`
 * is delivered to [onTick]; the terminal `0` triggers [onElapsed] (the reload).
 * Driven by [countdownSeconds]/`delay`, so virtual-time tests complete instantly.
 *
 * Created per-ViewModel rather than injected: it owns mutable per-VM [Job]
 * state and needs no collaborators, so DI would add ceremony without value.
 */
class RateLimitRetryController {
    private var job: Job? = null

    /**
     * Cancels any pending countdown, then starts a new one on [scope]:
     * calls [onTick] with `RATE_LIMIT_RETRY_SECONDS..1` and finally [onElapsed].
     */
    fun schedule(
        scope: CoroutineScope,
        onTick: (Int) -> Unit,
        onElapsed: () -> Unit,
    ) {
        job?.cancel()
        job =
            scope.launch {
                countdownSeconds(RATE_LIMIT_RETRY_SECONDS).collect { remaining ->
                    if (remaining > 0) onTick(remaining) else onElapsed()
                }
            }
    }

    /** Stops a pending countdown (e.g. when a manual retry pre-empts the auto-retry). */
    fun cancel() {
        job?.cancel()
        job = null
    }

    companion object {
        /** balldontlie's free tier limits requests per minute; 15 s is a safe wait. */
        const val RATE_LIMIT_RETRY_SECONDS = 15
    }
}
