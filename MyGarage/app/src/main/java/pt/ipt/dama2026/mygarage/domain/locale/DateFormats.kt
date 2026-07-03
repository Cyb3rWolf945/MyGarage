package pt.ipt.dama2026.mygarage.domain.locale

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Shared date/time formatters used across the app.
 */
object DateFormats {
    val DATE_DISPLAY = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val DATE_MONTH_SHORT = SimpleDateFormat("dd MMM", Locale.getDefault())
    val DATE_TIME_DISPLAY = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val ISO_8601 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }
}
