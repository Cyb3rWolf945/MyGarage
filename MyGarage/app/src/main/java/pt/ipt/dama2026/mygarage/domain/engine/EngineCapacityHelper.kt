package pt.ipt.dama2026.mygarage.domain.engine

/**
 * Helper para normalizar a cilindrada do motor.
 *
 * Opções disponíveis: 1000, 1200, 1400, 1600, 2000, 2500, 3000, 3500, 4000 cc.
 * Aceita formatos como "1587", "1587cc" ou "1.6L" e converte para o valor
 * mais próximo da lista (ex.: "2000 cc").
 */
object EngineCapacityHelper {

    private val capacityOptions = listOf(
        1000, 1200, 1400, 1600,
        2000, 2500, 3000, 3500, 4000
    )

    /**
     * Arredonda a cilindrada para a opção mais próxima da lista.
     * Extrai o valor numérico do texto (ex.: "1.6L" → 1600, "1587cc" → 1587),
     * depois encontra a opção com menor diferença.
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
     * Extrai o valor numérico em cc de uma string de cilindrada.
     *
     * Lógica:
     * 1. Remove tudo o que não for dígito ou ponto (ex.: "1.6L" → "1.6", "1587cc" → "1587").
     * 2. Se contiver ".", assume que está em litros e multiplica por 1000 (ex.: "1.6" → 1600).
     * 3. Se não, assume que já está em cc (ex.: "1587" → 1587).
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
     * Confirma se uma string de cilindrada (ex.: "2000 cc") corresponde
     * a uma das opções da lista. Usado para validar input do utilizador
     * antes de guardar.
     */
    fun isValidOption(value: String): Boolean {
        return try {
            capacityOptions.any { "$it cc" == value }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Devolve a lista de cilindradas para mostrar no dropdown de adicionar ou editar veículos.
     */
    fun getAllOptions(): List<String> {
        return capacityOptions.map { "$it cc" }
    }
}
