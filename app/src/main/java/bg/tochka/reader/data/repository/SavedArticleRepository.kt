package bg.tochka.reader.data.repository

import bg.tochka.reader.data.local.SavedArticleDao
import bg.tochka.reader.domain.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedArticleRepository @Inject constructor(
    private val dao: SavedArticleDao,
) {
    fun observeSaved(): Flow<List<Article>> =
        dao.observeAll().map { entities -> entities.map { it.toArticle() } }

    fun observeSavedIds(): Flow<Set<Int>> =
        dao.observeSavedIds().map { it.toSet() }

    suspend fun isSaved(articleId: Int): Boolean = dao.getById(articleId) != null

    suspend fun getById(articleId: Int): Article? = dao.getById(articleId)?.toArticle()

    suspend fun save(article: Article) {
        dao.insert(article.toSavedEntity(savedAtMillis = System.currentTimeMillis()))
    }

    suspend fun unsave(articleId: Int) {
        dao.deleteById(articleId)
    }

    suspend fun toggle(article: Article) {
        if (isSaved(article.id)) unsave(article.id) else save(article)
    }
}
