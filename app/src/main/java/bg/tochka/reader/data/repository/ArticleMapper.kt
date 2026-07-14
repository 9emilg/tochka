package bg.tochka.reader.data.repository

import bg.tochka.reader.data.local.SavedArticleEntity
import bg.tochka.reader.data.remote.dto.PostDto
import bg.tochka.reader.domain.model.Article
import bg.tochka.reader.domain.model.CategoryNames
import bg.tochka.reader.util.estimateReadMinutes
import bg.tochka.reader.util.formatWpDate
import bg.tochka.reader.util.htmlToPlainText

fun PostDto.toArticle(): Article {
    val categoryNames = embedded?.terms
        ?.flatten()
        ?.filter { it.taxonomy == "category" }
        ?.map { CategoryNames.displayName(it.slug, fallback = it.name.htmlToPlainText()) }
        ?: emptyList()

    val authorName = embedded?.author?.firstOrNull()?.name?.htmlToPlainText().orEmpty()
    val imageUrl = embedded?.featuredMedia?.firstOrNull()?.sourceUrl

    return Article(
        id = id,
        slug = slug,
        link = link,
        title = title.rendered.htmlToPlainText(),
        excerpt = excerpt.rendered.htmlToPlainText(),
        contentHtml = content.rendered,
        author = authorName,
        dateIso = date,
        dateDisplay = formatWpDate(date),
        categories = categoryNames,
        primaryCategory = categoryNames.firstOrNull(),
        imageUrl = imageUrl,
        readMinutes = estimateReadMinutes(content.rendered),
    )
}

fun Article.toSavedEntity(savedAtMillis: Long): SavedArticleEntity = SavedArticleEntity(
    id = id,
    slug = slug,
    link = link,
    title = title,
    excerpt = excerpt,
    contentHtml = contentHtml,
    author = author,
    dateIso = dateIso,
    dateDisplay = dateDisplay,
    categoriesCsv = categories.joinToString("|"),
    primaryCategory = primaryCategory,
    imageUrl = imageUrl,
    readMinutes = readMinutes,
    savedAtMillis = savedAtMillis,
)

fun SavedArticleEntity.toArticle(): Article = Article(
    id = id,
    slug = slug,
    link = link,
    title = title,
    excerpt = excerpt,
    contentHtml = contentHtml,
    author = author,
    dateIso = dateIso,
    dateDisplay = dateDisplay,
    categories = categoriesCsv.split("|").filter { it.isNotBlank() },
    primaryCategory = primaryCategory,
    imageUrl = imageUrl,
    readMinutes = readMinutes,
    savedAtMillis = savedAtMillis,
)
