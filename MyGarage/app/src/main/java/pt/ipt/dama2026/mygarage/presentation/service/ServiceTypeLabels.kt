package pt.ipt.dama2026.mygarage.presentation.service

/**
 * Shared service type labels used across screens, dialogs, and ViewModels.
 */
object ServiceTypeLabels {

    /**
     * Resolves a localized display label for a service log type.
     * Handles "revision", "inspection", and "regular".
     */
    fun labelFor(type: String, revisionRes: Int, inspectionRes: Int, regularRes: Int): Int = when (type.lowercase()) {
        "revision"   -> revisionRes
        "inspection" -> inspectionRes
        else         -> regularRes
    }
}
