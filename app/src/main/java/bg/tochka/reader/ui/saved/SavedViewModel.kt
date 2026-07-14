package bg.tochka.reader.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.tochka.reader.data.repository.SavedArticleRepository
import bg.tochka.reader.domain.model.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val savedArticleRepository: SavedArticleRepository,
) : ViewModel() {

    val savedArticles: StateFlow<List<Article>> = savedArticleRepository.observeSaved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun unsave(article: Article) {
        viewModelScope.launch { savedArticleRepository.unsave(article.id) }
    }
}
