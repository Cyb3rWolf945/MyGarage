package pt.ipt.dama2026.mygarage.domain.locale

import java.text.NumberFormat
import java.util.Locale

/**
 * Converts between canonical km and display/input units based on user preference.
 * Room stores all values in km. Display converts to miles if preference is MILES.
 */
object DistanceFormatter {

    private const val KM_TO_MILES = 0.621371

    /**
     * Converts km to display value for given unit.
     * MILES: km * KM_TO_MILES. KILOMETERS: as-is.
     */
    fun forDisplay(kilometers: Double, resolvedUnit: String): Double {
        return if (resolvedUnit == "MILES") kilometers * KM_TO_MILES else kilometers
    }

    /**
     * Converts user-input value back to canonical km.
     * MILES: value / KM_TO_MILES. KILOMETERS: as-is.
     */
    fun forStorage(userValue: Double, resolvedUnit: String): Double {
        return if (resolvedUnit == "MILES") userValue / KM_TO_MILES else userValue
    }

    /**
     * Formats a distance as human-readable with unit label, e.g. "12,450 km".
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
