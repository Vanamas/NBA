package cz.vanama.courtflow.core.common.time

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds

/**
 * Emits [from], [from] - 1, ..., 1, 0 with one second between emissions,
 * then completes. Driven by [delay], so tests with a virtual-time
 * dispatcher complete instantly.
 */
fun countdownSeconds(from: Int): Flow<Int> =
    flow {
        require(from >= 0) { "from must be >= 0, was $from" }
        var remaining = from
        while (remaining > 0) {
            emit(remaining)
            delay(1.seconds)
            remaining--
        }
        emit(0)
    }
