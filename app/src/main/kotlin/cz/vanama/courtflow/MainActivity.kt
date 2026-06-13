package cz.vanama.courtflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.navigation.CourtFlowNavGraph
import cz.vanama.courtflow.navigation.DeepLink

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CourtFlowTheme(dynamicColor = true) {
                CourtFlowNavGraph(initialBackStack = DeepLink.initialBackStack(intent?.data))
            }
        }
    }
}
