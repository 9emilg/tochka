package bg.tochka.reader.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.tochka.reader.data.repository.ArticleRepository
import bg.tochka.reader.data.repository.SavedArticleRepository
import bg.tochka.reader.domain.model.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Article> = emptyList(),
    val savedIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val savedArticleRepository: SavedArticleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            savedArticleRepository.observeSavedIds().collect { ids ->
                _uiState.update { it.copy(savedIds = ids) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), hasSearched = false, isLoading = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.update { it.copy(isLoading = true) }
            try {
                val results = articleRepository.searchPosts(query)
                _uiState.update { it.copy(isLoading = false, results = results, hasSearched = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, results = emptyList(), hasSearched = true) }
            }
        }
    }

    fun toggleSave(article: Article) {
        viewModelScope.launch { savedArticleRepository.toggle(article) }
    }
}
