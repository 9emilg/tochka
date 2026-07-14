package bg.tochka.reader.ui.article

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bg.tochka.reader.R
import bg.tochka.reader.ui.components.ArticleThumbnail
import bg.tochka.reader.ui.components.CategoryKicker

@Composable
fun ArticleScreen(
    onBack: () -> Unit,
    viewModel: ArticleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.article_back_cd))
            }
            Row {
                val article = uiState.article
                IconButton(onClick = {
                    if (article != null) {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.link}")
                        }
                        context.startActivity(
                            Intent.createChooser(sendIntent, context.getString(R.string.article_share_chooser_title)),
                        )
                    }
                }) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.article_share_cd))
                }
                IconButton(onClick = viewModel::toggleSave) {
                    Icon(
                        imageVector = if (uiState.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = stringResource(
                            if (uiState.isSaved) R.string.article_bookmark_remove_cd else R.string.article_bookmark_add_cd,
                        ),
                        tint = if (uiState.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.error || uiState.article == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.home_error_generic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                val article = uiState.article!!
                val textColor = MaterialTheme.colorScheme.onSurface
                val linkColor = MaterialTheme.colorScheme.primary
                val bodyFontSizePx = with(MaterialTheme.typography.bodyLarge.fontSize) { value }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp),
                ) {
                    article.primaryCategory?.let { CategoryKicker(text = it, modifier = Modifier.padding(top = 8.dp)) }
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(top = 11.dp, bottom = 14.dp),
                    )
                    Row(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = article.author,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "  ·  ${article.dateDisplay}  ·  ${
                                stringResource(R.string.article_read_minutes, article.readMinutes)
                            }",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (article.imageUrl != null) {
                        ArticleThumbnail(
                            imageUrl = article.imageUrl,
                            contentDescription = article.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                        )
                        Text(
                            text = stringResource(R.string.article_photo_credit),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
                        )
                    }
                    ArticleWebView(
                        html = article.contentHtml,
                        textColor = textColor,
                        linkColor = linkColor,
                        bodyFontSizeSp = bodyFontSizePx,
                        modifier = Modifier.padding(bottom = 24.dp),
                    )
                }
            }
        }
    }
}
