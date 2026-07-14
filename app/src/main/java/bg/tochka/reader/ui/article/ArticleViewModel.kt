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

@HiltViewModel
class ArticleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val savedArticleRepository: SavedArticleRepository,
) : ViewModel() {

    private val articleId: Int = checkNotNull(savedStateHandle[Destinations.ARTICLE_ID_ARG])

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            savedArticleRepository.observeSavedIds().collect { ids ->
                _uiState.update { it.copy(isSaved = ids.contains(articleId)) }
            }
        }
        load()
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            try {
                // Read the local cache first so saved articles work fully offline.
                val cached = savedArticleRepository.getById(articleId)
                val article = cached ?: articleRepository.getPost(articleId)
                _uiState.update { it.copy(isLoading = false, article = article) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = true) }
            }
        }
    }

    fun toggleSave() {
        val article = _uiState.value.article ?: return
        viewModelScope.launch { savedArticleRepository.toggle(article) }
    }
}
