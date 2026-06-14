package cz.vanama.courtflow.feature.settings

/**
 * Abstraction over the per-app locale (AppCompat's application locales). Lets
 * [SettingsViewModel] read and change the language through the MVI loop without
 * touching the static `AppCompatDelegate` API directly, keeping language
 * selection unit-testable. The concrete implementation lives in the app module,
 * where the declared locale config (`res/xml/locales_config.xml`) is visible.
 */
interface AppLocaleController {
    /** BCP-47 language tag of the current app locale, or `""` when following the system. */
    fun currentLanguageTag(): String

    /** Language tags the app ships translations for, derived from the locale config. */
    fun supportedLanguageTags(): List<String>

    /** Applies [tag] as the app locale; `""` clears the override and follows the system. */
    fun setLanguage(tag: String)
}
