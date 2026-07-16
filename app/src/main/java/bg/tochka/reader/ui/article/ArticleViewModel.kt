package bg.tochka.reader.ui.article

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.tochka.reader.data.repository.ArticleRepository
import bg.tochka.reader.data.repository.SavedArticleRepository
import bg.tochka.reader.domain.model.Article
import bg.tochka.reader.ui.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleUiState(
    val article: Article? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = true,
    val error: Boolean = false,
)

/**
 * Backs one Article screen instance, which may page through several articles (the list the user
 * was browsing — a home feed tab, Saved, or search results) via a swipeable pager. Each article
 * in [articleIds] gets its own lazily-loaded, independently cached [ArticleUiState].
 */
@HiltViewModel
class ArticleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val savedArticleRepository: SavedArticleRepository,
) : ViewModel() {

    private val initialArticleId: Int = checkNotNull(savedStateHandle[Destinations.ARTICLE_ID_ARG])

    val articleIds: List<Int> = savedStateHandle.get<String>(Destinations.ARTICLE_IDS_ARG)
        ?.split(",")
        ?.mapNotNull { it.toIntOrNull() }
        ?.takeIf { it.isNotEmpty() }
        ?: listOf(initialArticleId)

    val initialIndex: Int = articleIds.indexOf(initialArticleId).coerceAtLeast(0)

    private val _pageStates = MutableStateFlow<Map<Int, ArticleUiState>>(emptyMap())
    val pageStates: StateFlow<Map<Int, ArticleUiState>> = _pageStates.asStateFlow()

    private var savedIds: Set<Int> = emptySet()

    init {
        viewModelScope.launch {
            savedArticleRepository.observeSavedIds().collect { ids ->
                savedIds = ids
                _pageStates.update { pages ->
                    pages.mapValues { (id, state) -> state.copy(isSaved = ids.contains(id)) }
                }
            }
        }
        ensureLoaded(articleIds[initialIndex])
    }

    /** Called when a page enters composition (current page, plus pager's preloaded neighbors). */
    fun ensureLoaded(id: Int) {
        if (_pageStates.value.containsKey(id)) return
        _pageStates.update { it + (id to ArticleUiState(isLoading = true, isSaved = savedIds.contains(id))) }
        viewModelScope.launch {
            try {
                // Read the local cache first so saved articles work fully offline.
                val cached = savedArticleRepository.getById(id)
                val article = cached ?: articleRepository.getPost(id)
                _pageStates.update {
                    it + (id to ArticleUiState(article = article, isLoading = false, isSaved = savedIds.contains(id)))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _pageStates.update { it + (id to ArticleUiState(isLoading = false, error = true, isSaved = savedIds.contains(id))) }
            }
        }
    }

    fun toggleSave(id: Int) {
        val article = _pageStates.value[id]?.article ?: return
        viewModelScope.launch { savedArticleRepository.toggle(article) }
    }
}
