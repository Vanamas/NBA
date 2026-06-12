package cz.vanama.courtflow.domain.error

/**
 * Domain-level failure thrown by repository implementations when fetching data fails,
 * so presentation layers never depend on transport-specific exception types.
 */
class DataException(
    message: String?,
    cause: Throwable? = null,
) : Exception(message, cause)
