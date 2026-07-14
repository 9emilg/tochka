package bg.tochka.reader.domain.model

data class Category(
    val id: Int,
    val slug: String,
    val displayName: String,
)

/**
 * Real WordPress category slugs mapped to their Bulgarian display names.
 * Order here also defines the horizontal tab order on the home feed.
 */
object CategoryNames {
    const val THREE_MINUTES_SLUG = "tri-minuti"

    val homeTabSlugs = listOf(
        "novini",
        "istorii",
        "analizi",
        "razsledvaniya",
        "tri-minuti",
        "video",
    )

    private val displayNames = mapOf(
        "novini" to "Новини",
        "istorii" to "Истории",
        "analizi" to "Анализи",
        "razsledvaniya" to "Разследвания",
        "tri-minuti" to "Три минути",
        "video" to "Видео",
        "studio-balgariya" to "Студио България",
        "golemiyat-vapros" to "Големият въпрос",
        "golyamata-kartina" to "Голямата картина",
    )

    fun displayName(slug: String, fallback: String): String = displayNames[slug] ?: fallback
}
