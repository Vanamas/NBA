package cz.vanama.courtflow.data.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * The offline-first cache policy: a resource is refetched once its stored
 * `lastFetchedAt` is older than [TTL]. A `null` timestamp (never fetched) is
 * always stale.
 */
object CachePolicy {
    /** Uniform time-to-live for every cached resource. */
    val TTL: Duration = 1.days

    fun isStale(
        lastFetchedAt: Long?,
        now: Long,
    ): Boolean = lastFetchedAt == null || now - lastFetchedAt >= TTL.inWholeMilliseconds
}
