package cz.vanama.courtflow.core.designsystem.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cz.vanama.courtflow.core.designsystem.R

/**
 * Maps an API position code (`G`, `F`, `C`, `G-F`, `F-C`) to its full
 * human-readable label; unknown codes are returned unchanged.
 */
@Composable
fun positionLabel(position: String): String =
    when (position) {
        "G" -> stringResource(R.string.position_guard)
        "F" -> stringResource(R.string.position_forward)
        "C" -> stringResource(R.string.position_center)
        "G-F" -> stringResource(R.string.position_guard_forward)
        "F-C" -> stringResource(R.string.position_forward_center)
        else -> position
    }
