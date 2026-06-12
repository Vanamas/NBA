package cz.vanama.courtflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import cz.vanama.courtflow.navigation.CourtFlowNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CourtFlowTheme {
                CourtFlowNavGraph()
            }
        }
    }
}
