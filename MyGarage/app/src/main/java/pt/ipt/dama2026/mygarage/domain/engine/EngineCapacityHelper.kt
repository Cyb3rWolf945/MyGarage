package pt.ipt.dama2026.mygarage.domain.engine

/**
 * Utility for working with engine capacity (cilindrada) values.
 * Provides helpers to round raw CC values to the nearest standardized option.
 */
object EngineCapacityHelper {

    private val capacityOptions = listOf(
        1000, 1200, 1400, 1600,
        2000, 2500, 3000, 3500, 4000
    )

    /**
     * Rounds a raw engine capacity value to the nearest option.
     *
     * @param rawValue Engine capacity value as a string (e.g., "1587", "1587cc", "1.6L")
     * @return The closest capacity option formatted as "XXXX cc", or the original value if parsing fails
     */
    fun roundToNearestOption(rawValue: String): String {
        return try {
            if (rawValue.isBlank()) return ""

            val numeric = extractNumeric(rawValue)
            if (numeric <= 0) return rawValue

            val nearest = capacityOptions.minByOrNull { kotlin.math.abs(it - numeric) }
            if (nearest != null) "$nearest cc" else rawValue
        } catch (e: Exception) {
            rawValue
        }
    }

    /**
     * Extracts the numeric part from an engine capacity string.
     * Handles formats like "1587", "1587cc", "1.6L", "1600 cc"
     */
    private fun extractNumeric(value: String): Int {
        try {
            val cleaned = value.lowercase()
                .replace(Regex("[^\\d.]"), "")
                .takeIf { it.isNotEmpty() } ?: return 0

            return when {
                // If it has a dot, assume it's in liters (e.g., "1.6L" -> 1600cc)
                cleaned.contains(".") -> {
                    val liters = cleaned.toDouble()
                    (liters * 1000).toInt()
                }
                // Otherwise assume it's already in cc
                else -> cleaned.toInt()
            }
        } catch (e: Exception) {
            return 0
        }
    }

    /**
     * Validates that a value is one of the supported options.
     */
    fun isValidOption(value: String): Boolean {
        return try {
            capacityOptions.any { "$it cc" == value }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns all available capacity options formatted as strings.
     */
    fun getAllOptions(): List<String> {
        return capacityOptions.map { "$it cc" }
    }
}
