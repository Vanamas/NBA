package cz.vanama.courtflow.feature.settings

/**
 * Abstraction over app-module concerns the settings feature can't reach
 * directly: the app's [versionName] (which lives in the *app* module's
 * `BuildConfig`, not the feature module's) and launching the generated
 * open-source-licenses screen. Mirrors [AppLocaleController]; the concrete
 * implementation lives in the app module, where `BuildConfig.VERSION_NAME`
 * and `OssLicensesMenuActivity` are visible. Keeps [SettingsViewModel]
 * unit-testable without Android framework types.
 */
interface AppInfoProvider {
    /** The app's user-facing version name, e.g. `"1.0"` (from `BuildConfig.VERSION_NAME`). */
    val versionName: String

    /** Launches the auto-generated OSS-licenses screen (`OssLicensesMenuActivity`). */
    fun openOssLicenses()
}
