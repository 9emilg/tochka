package bg.tochka.reader.ui.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bg.tochka.reader.R
import bg.tochka.reader.domain.model.Article
import bg.tochka.reader.ui.components.ArticleListItem
import bg.tochka.reader.ui.theme.LocalTochkaFontScale
import bg.tochka.reader.ui.theme.tochkaScreenTitleStyle
import bg.tochka.reader.util.formatSavedAt

@Composable
fun SavedScreen(
    onArticleClick: (Article) -> Unit,
    onBrowseHome: () -> Unit,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val savedArticles by viewModel.savedArticles.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(22.dp)) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
            Text(
                text = stringResource(R.string.saved_title),
                style = tochkaScreenTitleStyle(LocalTochkaFontScale.current),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        if (savedArticles.isEmpty()) {
            EmptyState(onBrowseHome)
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 22.dp)) {
                items(savedArticles, key = { it.id }) { article ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ArticleListItem(
                        article = article,
                        metaLine = article.savedAtMillis?.let { formatSavedAt(it) } ?: article.dateDisplay,
                        onClick = { onArticleClick(article) },
                        isSaved = true,
                        onToggleSave = { viewModel.unsave(article) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onBrowseHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(66.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            text = stringResource(R.string.saved_empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = stringResource(R.string.saved_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
        Button(
            onClick = onBrowseHome,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text(stringResource(R.string.saved_empty_cta))
        }
    }
}
