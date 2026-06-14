package cz.vanama.courtflow.locale

import android.content.Context
import android.content.res.XmlResourceParser
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import cz.vanama.courtflow.R
import cz.vanama.courtflow.feature.settings.AppLocaleController
import org.xmlpull.v1.XmlPullParser

/**
 * [AppLocaleController] over AppCompat's application locales. Lives in the app
 * module because it derives the supported languages from the declared
 * `res/xml/locales_config.xml` — the same single source the manifest points at,
 * so the settings picker stays in sync as translations are added. Parsing the
 * resource (rather than `android.app.LocaleConfig`) keeps it working on the
 * full minSdk range.
 */
class DefaultAppLocaleController(
    private val context: Context,
) : AppLocaleController {
    override fun currentLanguageTag(): String =
        AppCompatDelegate
            .getApplicationLocales()
            .toLanguageTags()
            .substringBefore(',')
            .substringBefore('-')

    override fun supportedLanguageTags(): List<String> {
        val parser = context.resources.getXml(R.xml.locales_config)
        return try {
            parser.readLocaleTags()
        } finally {
            parser.close()
        }
    }

    private fun XmlResourceParser.readLocaleTags(): List<String> {
        val tags = mutableListOf<String>()
        var event = eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && name == "locale") {
                getAttributeValue(ANDROID_NS, "name")
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { tags.add(it.substringBefore('-')) }
            }
            event = next()
        }
        return tags
    }

    override fun setLanguage(tag: String) {
        AppCompatDelegate.setApplicationLocales(
            if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag),
        )
    }

    private companion object {
        const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    }
}
