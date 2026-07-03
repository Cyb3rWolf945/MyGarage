package pt.ipt.dama2026.mygarage.presentation.locale

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Handles dynamic app-language switching via AppCompatDelegate.
 */
object LocaleManager {

    /**
     * Applies BCP-47 language tag. Pass "SYSTEM" or empty to revert to OS default.
     */
    fun applyLanguage(languageTag: String?) {
        val tag = when (languageTag?.uppercase()) {
            "ENGLISH"    -> "en"
            "PORTUGUESE" -> "pt-PT"
            "SYSTEM"     -> ""
            "EN"         -> "en"
            "PT"         -> "pt-PT"
            "PT-PT"      -> "pt-PT"
            null         -> ""
            else         -> ""
        }
        val localeList = if (tag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * Resolves distance unit from preference + OS locale.
     * Returns "KILOMETERS" or "MILES".
     */
    fun resolveDistanceUnit(distanceUnit: String, context: Context): String {
        if (distanceUnit == "KILOMETERS") return "KILOMETERS"
        if (distanceUnit == "MILES") return "MILES"
        val osLocale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        return if (osLocale.language.equals("pt", ignoreCase = true)) "KILOMETERS" else "MILES"
    }

    /**
     * Returns unit label: "km" or "mi".
     */
    fun unitLabel(resolvedUnit: String): String {
        return if (resolvedUnit == "KILOMETERS") "km" else "mi"
    }
}
