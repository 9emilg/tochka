package bg.tochka.reader.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val bulgarian = Locale.forLanguageTag("bg")
private val shortFormatter = DateTimeFormatter.ofPattern("d MMMM", bulgarian)
private val longFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", bulgarian)

/** WordPress `date` field looks like "2026-07-13T17:55:09" (no offset, site-local time). */
fun formatWpDate(isoLocal: String, includeYear: Boolean = false): String = try {
    val dateTime = LocalDateTime.parse(isoLocal.substringBefore("+"))
    if (includeYear) dateTime.format(longFormatter) else dateTime.format(shortFormatter)
} catch (e: Exception) {
    isoLocal
}

fun formatSavedAt(savedAtMillis: Long): String {
    val savedDate = Instant.ofEpochMilli(savedAtMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    return when (today.toEpochDay() - savedDate.toEpochDay()) {
        0L -> "Запазено днес"
        1L -> "Запазено вчера"
        else -> "Запазено на ${savedDate.format(shortFormatter)}"
    }
}
