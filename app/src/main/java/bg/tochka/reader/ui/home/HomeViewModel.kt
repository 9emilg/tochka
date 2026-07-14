package bg.tochka.reader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.tochka.reader.data.repository.ArticleRepository
import bg.tochka.reader.data.repository.SavedArticleRepository
import bg.tochka.reader.domain.model.Article
import bg.tochka.reader.domain.model.CategoryNames
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 20

data class HomeUiState(
    val selectedCategory: String = CategoryNames.homeTabSlugs.first(),
    val articles: List<Article> = emptyList(),
    val savedIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: Boolean = false,
    val page: Int = 1,
    val endReached: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val savedArticleRepository: SavedArticleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            savedArticleRepository.observeSavedIds().collect { ids ->
                _uiState.update { it.copy(savedIds = ids) }
            }
        }
        loadCategory(_uiState.value.selectedCategory)
    }

    fun selectCategory(slug: String) {
        if (slug == _uiState.value.selectedCategory) return
        loadCategory(slug)
    }

    fun refresh() = loadCategory(_uiState.value.selectedCategory)

    /**
     * The first tab ("Новини") is the aggregate/mixed feed — a "bit of everything" rather than
     * posts strictly tagged with that category — matching the original design, where the featured
     * story and list items shown under it belong to several different categories at once.
     */
    private fun categoryFilterFor(tabSlug: String): String? =
        if (tabSlug == CategoryNames.homeTabSlugs.first()) null else tabSlug

    private fun loadCategory(slug: String) {
        _uiState.update {
            it.copy(
                selectedCategory = slug,
                isLoading = true,
                error = false,
                articles = emptyList(),
                page = 1,
                endReached = false,
            )
        }
        viewModelScope.launch {
            try {
                val articles = articleRepository.getPosts(
                    categorySlug = categoryFilterFor(slug),
                    page = 1,
                    perPage = PAGE_SIZE,
                )
                _uiState.update {
                    it.copy(isLoading = false, articles = articles, endReached = articles.size < PAGE_SIZE)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = true) }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.endReached) return
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            try {
                val nextPage = state.page + 1
                val more = articleRepository.getPosts(
                    categorySlug = categoryFilterFor(state.selectedCategory),
                    page = nextPage,
                    perPage = PAGE_SIZE,
                )
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        articles = it.articles + more,
                        page = nextPage,
                        endReached = more.isEmpty(),
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun toggleSave(article: Article) {
        viewModelScope.launch { savedArticleRepository.toggle(article) }
    }
}
