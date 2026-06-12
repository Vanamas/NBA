package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.designsystem.R

/** Resolves a classified data-layer failure to a localized message. */
@Composable
@ReadOnlyComposable
fun errorMessage(kind: DataErrorKind?): String =
    stringResource(
        when (kind) {
            DataErrorKind.NETWORK -> R.string.error_network
            DataErrorKind.RATE_LIMITED -> R.string.error_rate_limited
            DataErrorKind.NOT_FOUND -> R.string.error_not_found
            DataErrorKind.SERVER -> R.string.error_server
            DataErrorKind.UNKNOWN, null -> R.string.error_unknown
        },
    )

/** Convenience overload for paging errors carried as a plain [Throwable]. */
@Composable
@ReadOnlyComposable
fun errorMessage(error: Throwable): String = errorMessage((error as? DataException)?.kind)
