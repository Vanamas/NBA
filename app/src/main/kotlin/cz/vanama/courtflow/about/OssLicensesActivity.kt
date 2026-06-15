package cz.vanama.courtflow.about

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import cz.vanama.courtflow.R
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

/**
 * In-app fallback OSS-licenses screen listing the major open-source
 * libraries used by CourtFlow. This replaces [com.google.android.gms.oss.licenses.OssLicensesMenuActivity]
 * when the `oss-licenses-plugin` is not available (e.g. offline build environments).
 * The list is manually curated; use the Google OSS plugin in CI/CD for an auto-generated version.
 */
@OptIn(ExperimentalMaterial3Api::class)
class OssLicensesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CourtFlowTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(getString(R.string.oss_licenses_title)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = getString(R.string.oss_licenses_back),
                                    )
                                }
                            },
                        )
                    },
                ) { padding ->
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(padding),
                    ) {
                        items(OSS_LIBRARIES) { library ->
                            ListItem(
                                headlineContent = { Text(library.name) },
                                supportingContent = { Text(library.license) },
                            )
                        }
                    }
                }
            }
        }
    }

    private companion object {
        data class OssLibrary(
            val name: String,
            val license: String,
        )

        val OSS_LIBRARIES =
            listOf(
                OssLibrary("Kotlin", "Apache License 2.0"),
                OssLibrary("Jetpack Compose", "Apache License 2.0"),
                OssLibrary("Retrofit", "Apache License 2.0"),
                OssLibrary("OkHttp", "Apache License 2.0"),
                OssLibrary("Moshi", "Apache License 2.0"),
                OssLibrary("Glide", "BSD, MIT, Apache License 2.0"),
                OssLibrary("Koin", "Apache License 2.0"),
                OssLibrary("AndroidX Paging", "Apache License 2.0"),
                OssLibrary("AndroidX Room", "Apache License 2.0"),
                OssLibrary("AndroidX Navigation", "Apache License 2.0"),
                OssLibrary("Kotlin Coroutines", "Apache License 2.0"),
                OssLibrary("Timber", "Apache License 2.0"),
            )
    }
}
