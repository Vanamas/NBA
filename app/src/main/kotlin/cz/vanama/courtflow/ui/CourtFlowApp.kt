package cz.vanama.courtflow.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.vanama.courtflow.core.common.settings.ThemeMode
import cz.vanama.courtflow.core.common.settings.ThemePreferences
import cz.vanama.courtflow.core.common.settings.ThemePreferencesStore
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.navigation.CourtFlowNavGraph
import cz.vanama.courtflow.navigation.Destination
import org.koin.compose.koinInject

/**
 * App root: resolves the persisted [ThemePreferences] into the [CourtFlowTheme]
 * parameters, then renders the navigation graph. Defaults preserve the prior
 * behaviour (dynamic color on, follow system) until DataStore emits.
 */
@Composable
fun CourtFlowApp(initialBackStack: List<Destination>) {
    val store: ThemePreferencesStore = koinInject()
    val prefs by store.themePreferences.collectAsStateWithLifecycle(ThemePreferences())

    CourtFlowTheme(
        darkTheme = resolveDarkTheme(prefs.themeMode),
        dynamicColor = prefs.dynamicColor,
        trueBlack = prefs.trueBlack,
    ) {
        CourtFlowNavGraph(initialBackStack = initialBackStack)
    }
}

/**
 * Maps a persisted [ThemeMode] to the boolean dark flag [CourtFlowTheme] expects:
 * an explicit LIGHT/DARK choice wins; SYSTEM follows the device setting.
 */
@Composable
internal fun resolveDarkTheme(themeMode: ThemeMode): Boolean =
    when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
