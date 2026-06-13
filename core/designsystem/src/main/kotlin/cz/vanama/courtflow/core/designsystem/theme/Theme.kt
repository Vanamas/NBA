package cz.vanama.courtflow.core.designsystem.theme

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * App theme. [dynamicColor] opts into the wallpaper-derived Material You
 * palette on Android 12+; [trueBlack] swaps the dark scheme for a pure-black
 * AMOLED variant. Both default to false so previews and screenshot goldens
 * stay on the deterministic brand schemes — only the app entry point opts in.
 * When dynamic color is off, the static brand palette also lifts to the
 * medium/high-contrast variant per the Android 14+ system contrast setting.
 */
@Composable
fun CourtFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    trueBlack: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                remember(context, darkTheme) {
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }
            else ->
                staticColorScheme(
                    darkTheme = darkTheme,
                    contrast = systemContrast(context),
                    trueBlack = trueBlack,
                )
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // The host is not an Activity in previews and screenshot tests - skip window styling there.
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

/** System "Increase contrast" level in `[-1, 1]`; 0 below Android 14. */
private fun systemContrast(context: Context): Float =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        context.getSystemService(UiModeManager::class.java)?.contrast ?: 0f
    } else {
        0f
    }
