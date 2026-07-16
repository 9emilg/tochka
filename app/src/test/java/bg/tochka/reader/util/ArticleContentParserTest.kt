package bg.tochka.reader.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleContentParserTest {

    // Real content.rendered fragment from svobodnatochka.bg post 17970 (2026-07), a "#shorts"
    // YouTube embed — WordPress renders these as a vertical (9:16) iframe, not the usual 16:9.
    private val realShortsEmbed = """
        <p class="wp-block-paragraph">Първи параграф с текст.</p>
        <p class="wp-block-paragraph"> </p>
        <figure class="wp-block-embed is-type-video is-provider-youtube wp-block-embed-youtube wp-embed-aspect-9-16 wp-has-aspect-ratio"><div class="wp-block-embed__wrapper">
        <span class="wpex-responsive-media"><iframe loading="lazy" title="&quot;Изпуснати възможности&quot;. Защо има протест в НАТФИЗ #shorts" width="563" height="1000" src="https://www.youtube.com/embed/q-WRXjyhlOw?feature=oembed"  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe></span>
        </div></figure>
        <p class="wp-block-paragraph">Параграф след embed-а.</p>
    """.trimIndent()

    // Real content.rendered fragment from post 17760 — a standard landscape (16:9) embed.
    private val realLandscapeEmbed = """
        <figure class="wp-block-embed is-type-video is-provider-youtube wp-block-embed-youtube wp-embed-aspect-16-9 wp-has-aspect-ratio"><div class="wp-block-embed__wrapper">
        <iframe loading="lazy" width="980" height="551" src="https://www.youtube.com/embed/wWTT8t5GQFU?feature=oembed" allowfullscreen></iframe>
        </div></figure>
    """.trimIndent()

    @Test
    fun `plain article with no embeds is a single Html segment`() {
        val html = "<p>Параграф едно.</p><p>Параграф две.</p>"
        val segments = parseArticleContent(html)

        assertEquals(1, segments.size)
        assertTrue(segments[0] is ContentSegment.Html)
    }

    @Test
    fun `shorts embed is extracted as YouTube with vertical aspect ratio, text preserved around it`() {
        val segments = parseArticleContent(realShortsEmbed)

        assertEquals(3, segments.size)
        assertTrue(segments[0] is ContentSegment.Html)
        assertTrue((segments[0] as ContentSegment.Html).html.contains("Първи параграф"))

        val youtube = segments[1] as ContentSegment.YouTube
        assertEquals("q-WRXjyhlOw", youtube.videoId)
        assertEquals(563f / 1000f, youtube.aspectRatio, 0.001f)
        assertTrue(youtube.aspectRatio < 1f) // portrait

        assertTrue(segments[2] is ContentSegment.Html)
        assertTrue((segments[2] as ContentSegment.Html).html.contains("Параграф след"))
    }

    @Test
    fun `landscape embed keeps a wide aspect ratio`() {
        val segments = parseArticleContent(realLandscapeEmbed)

        val youtube = segments.filterIsInstance<ContentSegment.YouTube>().single()
        assertEquals("wWTT8t5GQFU", youtube.videoId)
        assertEquals(980f / 551f, youtube.aspectRatio, 0.001f)
        assertTrue(youtube.aspectRatio > 1f) // landscape
    }

    @Test
    fun `missing width-height falls back to 16-9`() {
        val html = """<figure class="wp-block-embed-youtube"><iframe src="https://www.youtube.com/embed/abc12345678"></iframe></figure>"""
        val youtube = parseArticleContent(html).filterIsInstance<ContentSegment.YouTube>().single()

        assertEquals(16f / 9f, youtube.aspectRatio, 0.001f)
    }

    @Test
    fun `figcaption is picked up as the caption when present`() {
        val html = """
            <figure class="wp-block-embed-youtube">
              <iframe src="https://www.youtube.com/embed/abc12345678" width="16" height="9"></iframe>
              <figcaption>Видео надпис</figcaption>
            </figure>
        """.trimIndent()
        val youtube = parseArticleContent(html).filterIsInstance<ContentSegment.YouTube>().single()

        assertEquals("Видео надпис", youtube.caption)
    }

    @Test
    fun `non-YouTube embeds like Twitter are left as plain html, not misdetected`() {
        val html = """<figure class="wp-block-embed-twitter"><iframe src="https://platform.twitter.com/embed/Tweet.html"></iframe></figure>"""
        val segments = parseArticleContent(html)

        assertEquals(1, segments.size)
        assertTrue(segments[0] is ContentSegment.Html)
    }

    @Test
    fun `youtu-be short links are recognized`() {
        val html = """<p><a href="https://youtu.be/dQw4w9WgXcQ">link</a></p>"""
        val youtube = parseArticleContent(html).filterIsInstance<ContentSegment.YouTube>().singleOrNull()

        assertEquals("dQw4w9WgXcQ", youtube?.videoId)
    }

    @Test
    fun `no caption is null, not a crash`() {
        val youtube = parseArticleContent(realLandscapeEmbed).filterIsInstance<ContentSegment.YouTube>().single()
        assertNull(youtube.caption)
    }
}
