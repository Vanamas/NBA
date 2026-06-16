package cz.vanama.courtflow.data.cache

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.days

class CachePolicyTest {
    @Test
    fun `the ttl is one day`() {
        assertTrue(CachePolicy.TTL == 1.days)
    }

    @Test
    fun `cache is stale when it was never fetched`() {
        assertTrue(CachePolicy.isStale(lastFetchedAt = null, now = 1_000L))
    }

    @Test
    fun `cache is fresh within the ttl`() {
        val now = 10.days.inWholeMilliseconds
        val halfTtl = CachePolicy.TTL.inWholeMilliseconds / 2
        val lastFetchedAt = now - halfTtl

        assertFalse(CachePolicy.isStale(lastFetchedAt, now))
    }

    @Test
    fun `cache is fresh when the timestamp is in the future (clock skew)`() {
        val now = 5.days.inWholeMilliseconds
        val lastFetchedAt = 6.days.inWholeMilliseconds

        assertFalse(CachePolicy.isStale(lastFetchedAt, now))
    }

    @Test
    fun `cache is stale once the ttl has elapsed`() {
        val now = 10.days.inWholeMilliseconds
        val lastFetchedAt = now - CachePolicy.TTL.inWholeMilliseconds

        assertTrue(CachePolicy.isStale(lastFetchedAt, now))
    }
}
