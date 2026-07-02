package pt.ipt.dama2026.mygarage.domain.engine

/**
 * Rounds raw engine capacity values to nearest standardized option (cc).
 */
object EngineCapacityHelper {

    private val capacityOptions = listOf(
        1000, 1200, 1400, 1600,
        2000, 2500, 3000, 3500, 4000
    )

    /**
     * Rounds raw engine capacity string to nearest standard option.
     * Handles formats: "1587", "1587cc", "1.6L".
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
     * Extracts numeric cc value from engine capacity string.
     */
    private fun extractNumeric(value: String): Int {
        try {
            val cleaned = value.lowercase()
                .replace(Regex("[^\\d.]"), "")
                .takeIf { it.isNotEmpty() } ?: return 0

            return when {
                cleaned.contains(".") -> {
                    val liters = cleaned.toDouble()
                    (liters * 1000).toInt()
                }
                else -> cleaned.toInt()
            }
        } catch (e: Exception) {
            return 0
        }
    }

    /**
     * Checks if value is one of the supported options.
     */
    fun isValidOption(value: String): Boolean {
        return try {
            capacityOptions.any { "$it cc" == value }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns all capacity options as display strings.
     */
    fun getAllOptions(): List<String> {
        return capacityOptions.map { "$it cc" }
    }
}
