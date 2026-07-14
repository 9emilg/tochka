package bg.tochka.reader.data.repository

import bg.tochka.reader.data.remote.WpApiService
import bg.tochka.reader.domain.model.Article
import bg.tochka.reader.domain.model.Category
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val api: WpApiService,
) {
    private val categoriesMutex = Mutex()
    private var cachedCategories: List<Category>? = null

    suspend fun getCategories(): List<Category> = categoriesMutex.withLock {
        cachedCategories ?: api.getCategories().map { dto ->
            Category(id = dto.id, slug = dto.slug, displayName = dto.name)
        }.also { cachedCategories = it }
    }

    suspend fun getCategoryId(slug: String): Int? =
        getCategories().firstOrNull { it.slug == slug }?.id

    suspend fun getPosts(categorySlug: String?, page: Int, perPage: Int = 20): List<Article> {
        val categoryId = categorySlug?.let { getCategoryId(it) }
        return api.getPosts(page = page, perPage = perPage, categoryId = categoryId)
            .map { it.toArticle() }
    }

    suspend fun searchPosts(query: String): List<Article> =
        api.searchPosts(query).map { it.toArticle() }

    suspend fun getPost(id: Int): Article = api.getPost(id).toArticle()
}
