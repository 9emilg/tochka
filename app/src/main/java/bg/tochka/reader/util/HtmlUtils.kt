package bg.tochka.reader.util

import android.text.Html

/** Strips tags and decodes entities from WordPress `rendered` HTML fragments (titles, excerpts). */
fun String.htmlToPlainText(): String =
    Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toString().trim()

/** Rough reading-time estimate from an HTML article body, ~200 words per minute. */
fun estimateReadMinutes(contentHtml: String): Int {
    val plain = Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_COMPACT).toString()
    val words = plain.split(Regex("\\s+")).count { it.isNotBlank() }
    return (words / 200).coerceAtLeast(1)
}
