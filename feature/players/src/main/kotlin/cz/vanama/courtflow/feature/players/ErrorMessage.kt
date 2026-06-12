package cz.vanama.courtflow.feature.players

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.designsystem.R as DesignR

/** Resolves a classified data-layer failure to a localized message. */
@Composable
@ReadOnlyComposable
internal fun errorMessage(kind: DataErrorKind?): String =
    stringResource(
        when (kind) {
            DataErrorKind.NETWORK -> DesignR.string.error_network
            DataErrorKind.RATE_LIMITED -> DesignR.string.error_rate_limited
            DataErrorKind.NOT_FOUND -> DesignR.string.error_not_found
            DataErrorKind.SERVER -> DesignR.string.error_server
            DataErrorKind.UNKNOWN, null -> DesignR.string.error_unknown
        },
    )

/** Convenience overload for paging errors carried as a plain [Throwable]. */
@Composable
@ReadOnlyComposable
internal fun errorMessage(error: Throwable): String = errorMessage((error as? DataException)?.kind)
