package bg.tochka.reader.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

sealed interface ContentSegment {
    data class Html(val html: String) : ContentSegment
    data class YouTube(val videoId: String, val caption: String?, val aspectRatio: Float) : ContentSegment
}

private const val DEFAULT_ASPECT_RATIO = 16f / 9f

private val youtubeUrlPatterns = listOf(
    Regex("""youtube(?:-nocookie)?\.com/embed/([a-zA-Z0-9_-]{6,})"""),
    Regex("""youtube\.com/watch\?v=([a-zA-Z0-9_-]{6,})"""),
    Regex("""youtu\.be/([a-zA-Z0-9_-]{6,})"""),
)

private fun youTubeIdFromUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    for (pattern in youtubeUrlPatterns) {
        pattern.find(url)?.let { return it.groupValues[1] }
    }
    return null
}

/**
 * A top-level node is treated as a YouTube embed if it (or something nested inside it — WordPress
 * wraps embeds in a `<figure>`/`<div>` block) contains a YouTube iframe or link, regardless of the
 * wrapper's own tag or class — this covers raw `<iframe>`, Gutenberg's `wp-block-embed-youtube`,
 * and anything else the API might hand back.
 */
private fun extractYouTubeEmbed(node: Node): ContentSegment.YouTube? {
    if (node !is Element) return null
    val iframe = if (node.tagName() == "iframe") node else node.selectFirst("iframe")
    val videoId = youTubeIdFromUrl(iframe?.attr("src"))
        ?: node.select("a[href]").firstNotNullOfOrNull { youTubeIdFromUrl(it.attr("href")) }
        ?: return null
    val caption = node.selectFirst("figcaption")?.text()?.takeIf { it.isNotBlank() }

    // WordPress embeds Shorts (vertical, 9:16) as often as regular (16:9) videos on this site —
    // read the iframe's own declared width/height rather than assuming landscape.
    val width = iframe?.attr("width")?.toFloatOrNull()
    val height = iframe?.attr("height")?.toFloatOrNull()
    val aspectRatio = if (width != null && height != null && width > 0 && height > 0) {
        width / height
    } else {
        DEFAULT_ASPECT_RATIO
    }

    return ContentSegment.YouTube(videoId, caption, aspectRatio)
}

/**
 * Splits WordPress `content.rendered` HTML into a sequence of plain-HTML chunks and native
 * YouTube-embed markers, in document order, so the article screen can render most of the body
 * in a WebView while swapping YouTube embeds for a native card at the exact same spot.
 */
fun parseArticleContent(rawHtml: String): List<ContentSegment> {
    val body = Jsoup.parseBodyFragment(rawHtml).body()
    val segments = mutableListOf<ContentSegment>()
    val htmlBuilder = StringBuilder()

    fun flushHtml() {
        val html = htmlBuilder.toString()
        if (html.isNotBlank()) segments += ContentSegment.Html(html)
        htmlBuilder.setLength(0)
    }

    for (node in body.childNodes().toList()) {
        val embed = extractYouTubeEmbed(node)
        if (embed != null) {
            flushHtml()
            segments += embed
        } else {
            htmlBuilder.append(node.outerHtml())
        }
    }
    flushHtml()
    return segments
}
