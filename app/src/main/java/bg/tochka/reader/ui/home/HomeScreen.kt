package bg.tochka.reader.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bg.tochka.reader.R
import bg.tochka.reader.domain.model.Article
import bg.tochka.reader.domain.model.CategoryNames
import bg.tochka.reader.ui.components.ArticleListItem
import bg.tochka.reader.ui.components.CategoryTabRow
import bg.tochka.reader.ui.components.FeaturedArticleCard
import bg.tochka.reader.ui.components.TabItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArticleClick: (Article) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            totalItems > 0 && lastVisible >= totalItems - 4
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(top = 6.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                Box(
                    modifier = Modifier
                        .padding(start = 3.dp, bottom = 6.dp)
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.home_search_cd))
            }
        }

        CategoryTabRow(
            tabs = CategoryNames.homeTabSlugs.map { slug -> TabItem(slug, CategoryNames.displayName(slug, slug)) },
            selectedSlug = uiState.selectedCategory,
            onSelect = viewModel::selectCategory,
            modifier = Modifier.padding(horizontal = 22.dp),
        )

        val isAggregateFeed = uiState.selectedCategory == CategoryNames.homeTabSlugs.first()

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.error && uiState.articles.isEmpty() -> {
                ErrorState(onRetry = viewModel::refresh)
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(uiState.articles, key = { _, article -> article.id }) { index, article ->
                            if (index == 0) {
                                FeaturedArticleCard(
                                    article = article,
                                    metaLine = "${article.author} · ${article.dateDisplay}",
                                    onClick = { onArticleClick(article) },
                                    kickerOverride = if (isAggregateFeed) {
                                        stringResource(R.string.home_leading_story)
                                    } else {
                                        null
                                    },
                                )
                            } else {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                ArticleListItem(
                                    article = article,
                                    metaLine = article.dateDisplay,
                                    onClick = { onArticleClick(article) },
                                    isSaved = uiState.savedIds.contains(article.id),
                                    onToggleSave = { viewModel.toggleSave(article) },
                                )
                            }
                        }
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.home_error_generic),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}
