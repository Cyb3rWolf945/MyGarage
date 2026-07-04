package pt.ipt.dama2026.mygarage.domain.locale

import java.text.NumberFormat
import java.util.Locale

/**
 * Converte distâncias entre quilómetros ( valor guardado na BD ) e a unidade escolhida
 * pelo utilizador (km ou milhas).
 *
 * Assim todos os cálculos usam km, tudo o que é ecrã usa o que o user prefere.
 *
 * 1 km = 0.621371 milhas.
 */
object DistanceFormatter {

    private const val KM_TO_MILES = 0.621371

    /** km → unidade do user. Se for milhas, multiplica por 0.62. */
    fun forDisplay(kilometers: Double, resolvedUnit: String): Double {
        return if (resolvedUnit == "MILES") kilometers * KM_TO_MILES else kilometers
    }

    /** Input do user → km. Se o user escreveu em milhas, divide por 0.62. */
    fun forStorage(userValue: Double, resolvedUnit: String): Double {
        return if (resolvedUnit == "MILES") userValue / KM_TO_MILES else userValue
    }

    /** Formata para mostrar no ecrã (ex.: "12450" → "12,450"). */
    fun formatDisplay(kilometers: Double, resolvedUnit: String): String {
        val converted = forDisplay(kilometers, resolvedUnit)
        return NumberFormat.getNumberInstance(Locale.US).format(converted.toLong())
    }

    /** Tira vírgulas e outros caracteres do input do user, devolve número (ex.: "12,450" → 12450.0). */
    fun parseUserInput(raw: String): Double {
        val clean = raw.replace(",", "").replace(Regex("[^0-9.]"), "")
        return clean.toDoubleOrNull() ?: 0.0
    }
}
