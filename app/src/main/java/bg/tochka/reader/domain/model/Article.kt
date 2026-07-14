package bg.tochka.reader.domain.model

data class Article(
    val id: Int,
    val slug: String,
    val link: String,
    val title: String,
    val excerpt: String,
    val contentHtml: String,
    val author: String,
    val dateIso: String,
    val dateDisplay: String,
    val categories: List<String>,
    val primaryCategory: String?,
    val imageUrl: String?,
    val readMinutes: Int,
    val savedAtMillis: Long? = null,
)
