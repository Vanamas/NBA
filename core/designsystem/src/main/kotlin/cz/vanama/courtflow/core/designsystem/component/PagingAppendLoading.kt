package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Inline next-page loading row appended below a paginated list. */
@Composable
fun PagingAppendLoading(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentWidth(Alignment.CenterHorizontally),
    )
}
