package pt.ipt.dama2026.mygarage.presentation.locale

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Gere a mudança de idioma da app e a unidade de distância.
 *
 * applyLanguage: aplica o idioma escolhido usando AppCompatDelegate.
 * resolveDistanceUnit: decide km ou mi com base na preferência ou no SO.
 * unitLabel: devolve "km" ou "mi" para mostrar na UI.
 */
object LocaleManager {

    /** Aplica o idioma. "SYSTEM" ou vazio → usa o idioma do telemóvel. */
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

    /** Decide km ou milhas: preferência do user → idioma do SO → MILES por defeito. */
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

    /** Devolve "km" para KILOMETERS, "mi" para MILES. */
    fun unitLabel(resolvedUnit: String): String {
        return if (resolvedUnit == "KILOMETERS") "km" else "mi"
    }
}
