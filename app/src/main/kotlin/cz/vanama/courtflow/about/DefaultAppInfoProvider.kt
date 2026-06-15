package cz.vanama.courtflow.about

import android.content.Context
import android.content.Intent
import cz.vanama.courtflow.BuildConfig
import cz.vanama.courtflow.feature.settings.AppInfoProvider

/**
 * [AppInfoProvider] backed by the app module's `BuildConfig` and an in-app
 * [OssLicensesActivity]. Lives in the app module because the feature module
 * can read neither the app's `VERSION_NAME` nor the licenses Activity.
 * Launched with `FLAG_ACTIVITY_NEW_TASK` because [context] is the application
 * context (no Activity on its back stack).
 *
 * Note: the preferred implementation uses Google's `oss-licenses-plugin`
 * and `OssLicensesMenuActivity` (auto-generated, always accurate). This
 * in-app fallback is used when the plugin cannot be resolved (e.g. offline
 * build environments). Swap the destination once the plugin is available.
 */
class DefaultAppInfoProvider(
    private val context: Context,
) : AppInfoProvider {
    override val versionName: String = BuildConfig.VERSION_NAME

    override fun openOssLicenses() {
        val intent =
            Intent(context, OssLicensesActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
