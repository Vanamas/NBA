package cz.vanama.courtflow.domain.error

/**
 * Domain-level failure thrown by repository implementations when fetching data fails,
 * so presentation layers never depend on transport-specific exception types. The
 * [kind] classifies the failure; [message] is debugging detail only and must never
 * be shown to the user.
 */
class DataException(
    val kind: DataErrorKind,
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
