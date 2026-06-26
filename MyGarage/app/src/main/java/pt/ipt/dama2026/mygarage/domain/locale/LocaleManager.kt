package pt.ipt.dama2026.mygarage.domain.locale

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Handles dynamic app-language switching exclusively via
 * [AppCompatDelegate.setApplicationLocales] — the only API
 * that Jetpack Compose honours for live language changes.
 */
object LocaleManager {

    /**
     * Applies the requested BCP-47 language tag to the app process.
     *
     * Pass `null`, an empty string, or `"SYSTEM"` to revert to the
     * device's OS default locale.
     *
     * @param languageTag A BCP-47 tag such as `"en"` or `"pt-PT"`,
     *                    or `"SYSTEM"` to clear the override.
     */
    fun applyLanguage(languageTag: String?) {
        // Defensive mapping: accept raw enum-style values OR clean BCP-47 tags
        val tag = when (languageTag?.uppercase()) {
            "ENGLISH"    -> "en"
            "PORTUGUESE" -> "pt-PT"
            "SYSTEM"     -> ""
            "EN"         -> "en"
            "PT"         -> "pt-PT"
            "PT-PT"      -> "pt-PT"
            null         -> ""
            else         -> "" // unknown → fallback to system default
        }
        val localeList = if (tag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * Resolves the effective distance unit from the stored preference
     * and the device's current OS locale.
     *
     * Rules:
     * - "KILOMETERS"   → Kilometers
     * - "MILES"        → Miles
     * - "SYSTEM"       → Kilometers if OS locale is pt-PT, else Miles
     *
     * @return "KILOMETERS" or "MILES".
     */
    fun resolveDistanceUnit(distanceUnit: String, context: Context): String {
        if (distanceUnit == "KILOMETERS") return "KILOMETERS"
        if (distanceUnit == "MILES") return "MILES"
        // "SYSTEM": use OS locale
        val osLocale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        return if (osLocale.language.equals("pt", ignoreCase = true)) "KILOMETERS" else "MILES"
    }

    /**
     * Returns a human-readable unit label for display (e.g. "km" or "mi").
     */
    fun unitLabel(resolvedUnit: String): String {
        return if (resolvedUnit == "KILOMETERS") "km" else "mi"
    }
}
