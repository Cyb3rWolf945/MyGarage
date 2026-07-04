package pt.ipt.dama2026.mygarage.domain.fuel

/**
 * Shared fuel type label resolution using canonical keys.
 * Keys are language-independent; display is resolved at UI time.
 */
object FuelTypeLabels {

    /** Maps a canonical key to a localized string resource ID. */
    fun labelFor(key: String, gasolineRes: Int, dieselRes: Int, electricRes: Int): Int = when (key.lowercase()) {
        "gasoline" -> gasolineRes
        "diesel"   -> dieselRes
        "electric" -> electricRes
        else       -> electricRes // fallback — shouldn't happen for new data
    }

    /** Converts a localized or API display string back to a canonical key. */
    fun canonicalKey(display: String): String = when (display.lowercase()) {
        "gasolina", "gasoline", "petrol", "gasóleo", "gasoleo" -> "gasoline"
        "diesel", "gasóleo", "gasoleo" -> "diesel"
        "elétrico", "electric", "eletrico" -> "electric"
        else -> display.lowercase()
    }
}
