package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * The app-wide loading spinner, pre-tagged with [TestTags.LOADING_INDICATOR]
 * so UI tests target one constant. Callers position it themselves (the detail
 * screens center it inside their content box).
 */
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier.testTag(TestTags.LOADING_INDICATOR))
}
