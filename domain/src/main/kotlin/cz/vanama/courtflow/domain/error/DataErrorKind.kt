package cz.vanama.courtflow.domain.error

/**
 * Coarse classification of a data-layer failure; the UI maps each kind
 * to a localized, human-readable message.
 */
enum class DataErrorKind {
    /** Connectivity / I/O failure before an HTTP response arrived. */
    NETWORK,

    /** HTTP 429 — the API rate limit was hit. */
    RATE_LIMITED,

    /** HTTP 404 — the requested entity does not exist. */
    NOT_FOUND,

    /** HTTP 5xx — the API is failing. */
    SERVER,

    /** Any other failure. */
    UNKNOWN,
}
