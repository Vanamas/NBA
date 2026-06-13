package cz.vanama.courtflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cz.vanama.courtflow.navigation.DeepLink
import cz.vanama.courtflow.ui.CourtFlowApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CourtFlowApp(initialBackStack = DeepLink.initialBackStack(intent?.data))
        }
    }
}
