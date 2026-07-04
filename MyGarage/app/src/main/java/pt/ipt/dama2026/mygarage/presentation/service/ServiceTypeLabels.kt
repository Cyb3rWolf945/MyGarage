package pt.ipt.dama2026.mygarage.presentation.service

/** Traduz o tipo de serviço ("revision", "inspection", "regular") para o ID da string localizada. */
object ServiceTypeLabels {

    fun labelFor(type: String, revisionRes: Int, inspectionRes: Int, regularRes: Int): Int = when (type.lowercase()) {
        "revision"   -> revisionRes
        "inspection" -> inspectionRes
        else         -> regularRes
    }
}
