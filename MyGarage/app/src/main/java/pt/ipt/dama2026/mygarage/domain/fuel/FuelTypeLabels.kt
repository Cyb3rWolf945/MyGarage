package pt.ipt.dama2026.mygarage.domain.fuel

/**
 * Ajuda a lidar com os tipos de combustível em inglês e português.
 *
 * No caso de querermos usar o dialog da câmara, a API das matriculas devolve em inglês o tipo de combustível (gasoline, diesel, electric).
 * Para que não seja necessário traduzir manualmente, este helper permite mapear os tipos de combustível entre inglês e português.
 *
 * O tipo de combustível é guardado na BD em inglês.
 */
object FuelTypeLabels {

    /**
     * Devolve o rótulo (string resource) correspondente ao tipo de combustível.
     */
    fun labelFor(key: String, gasolineRes: Int, dieselRes: Int, electricRes: Int): Int = when (key.lowercase()) {
        "gasoline" -> gasolineRes
        "diesel"   -> dieselRes
        "electric" -> electricRes
        else       -> electricRes
    }

    /**
     * Devolve a chave em inglês correspondente ao rótulo do tipo de combustível.
     */
    fun canonicalKey(display: String): String = when (display.lowercase()) {
        "gasolina", "gasoline", "petrol", "gasóleo", "gasoleo" -> "gasoline"
        "diesel", "gasóleo", "gasoleo" -> "diesel"
        "elétrico", "electric", "eletrico" -> "electric"
        else -> display.lowercase()
    }
}
