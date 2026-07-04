package pt.ipt.dama2026.mygarage.domain.locale

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Formatos de data/hora usados em toda a app.
 *
 * Centralizados aqui para evitar inconsistências — se um dia se mudar
 * o formato, muda-se num só sítio aplicando o princípio DRY (Don't Repeat Yourself).
 *
 * - DATE_DISPLAY: datas no ecrã (ex.: "15/03/2024").
 * - DATE_MONTH_SHORT: datas curtas (ex.: "15 mar").
 * - DATE_TIME_DISPLAY: data + hora (ex.: "15/03/2024 14:30").
 * - ISO_8601: transporte entre app e servidor (ex.: "2024-03-15T14:30:00.000Z").
 *   Sempre em UTC para evitar problemas de fuso horário.
 */
object DateFormats {
    val DATE_DISPLAY = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val DATE_MONTH_SHORT = SimpleDateFormat("dd MMM", Locale.getDefault())
    val DATE_TIME_DISPLAY = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val ISO_8601 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }
}
