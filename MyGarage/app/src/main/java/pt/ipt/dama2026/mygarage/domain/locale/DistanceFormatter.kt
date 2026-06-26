package pt.ipt.dama2026.mygarage.domain.locale

import java.text.NumberFormat
import java.util.Locale

/**
 * Stateless utility for converting between canonical storage units (Kilometers)
 * and display/input units based on the resolved user preference.
 *
 * **Canonical Rule:** The Room database and Domain entities store all mileage
 * values as [Double] representing **Kilometers**.
 */
object DistanceFormatter {

    /** 1 Kilometer = 0.621371 Miles */
    private const val KM_TO_MILES = 0.621371

    /**
     * Converts a canonical Kilometer value to the display value
     * according to [resolvedUnit].
     *
     * - "KILOMETERS" → returned as-is.
     * - "MILES"      → multiplied by [KM_TO_MILES].
     */
    fun forDisplay(kilometers: Double, resolvedUnit: String): Double {
        return if (resolvedUnit == "MILES") kilometers * KM_TO_MILES else kilometers
    }

    /**
     * Converts a user-input value (which may be in Miles) back to
     * canonical Kilometers for Room storage.
     *
     * - "KILOMETERS" → returned as-is.
     * - "MILES"      → divided by [KM_TO_MILES].
     */
    fun forStorage(userValue: Double, resolvedUnit: String): Double {
        return if (resolvedUnit == "MILES") userValue / KM_TO_MILES else userValue
    }

    /**
     * Formats a display distance value as a human-readable string
     * with thousands separators, rounded to 0 decimals for readability.
     *
     * Example output: "12,450 km" or "7,735 mi".
     */
    fun formatDisplay(kilometers: Double, resolvedUnit: String): String {
        val converted = forDisplay(kilometers, resolvedUnit)
        val unitLabel = LocaleManager.unitLabel(resolvedUnit)
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(converted.toLong())
        return "$formatted $unitLabel"
    }

    /**
     * Parses a user-entered numeric string (e.g. "12450" or "12,450")
     * and returns it as a [Double], stripping any non-numeric characters
     * except the decimal point.
     */
    fun parseUserInput(raw: String): Double {
        val clean = raw.replace(",", "").replace(Regex("[^0-9.]"), "")
        return clean.toDoubleOrNull() ?: 0.0
    }
}
