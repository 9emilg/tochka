package bg.tochka.reader.ui.article

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bg.tochka.reader.R
import bg.tochka.reader.ui.components.ArticleThumbnail
import bg.tochka.reader.ui.components.CategoryKicker
import bg.tochka.reader.ui.components.YouTubeEmbedCard
import bg.tochka.reader.util.ContentSegment
import bg.tochka.reader.util.parseArticleContent
import kotlinx.coroutines.launch

@Composable
fun ArticleScreen(
    onBack: () -> Unit,
    viewModel: ArticleViewModel = hiltViewModel(),
) {
    val pageStates by viewModel.pageStates.collectAsState()
    val pagerState = rememberPagerState(initialPage = viewModel.initialIndex) { viewModel.articleIds.size }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val currentId = viewModel.articleIds.getOrElse(pagerState.currentPage) { viewModel.articleIds.first() }
    val currentArticle = pageStates[currentId]?.article
    val currentIsSaved = pageStates[currentId]?.isSaved == true

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
                IconButton(onClick = {
                    val article = currentArticle ?: return@IconButton
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.link}")
                    }
                    context.startActivity(
                        Intent.createChooser(sendIntent, context.getString(R.string.article_share_chooser_title)),
                    )
                }) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.article_share_cd))
                }
                IconButton(onClick = { viewModel.toggleSave(currentId) }) {
                    Icon(
                        imageVector = if (currentIsSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = stringResource(
                            if (currentIsSaved) R.string.article_bookmark_remove_cd else R.string.article_bookmark_add_cd,
                        ),
                        tint = if (currentIsSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val id = viewModel.articleIds[page]
            LaunchedEffect(id) { viewModel.ensureLoaded(id) }
            val state = pageStates[id] ?: ArticleUiState()

            ArticlePageContent(
                state = state,
                previousTitle = pageStates[viewModel.articleIds.getOrNull(page - 1)]?.article?.title,
                nextTitle = pageStates[viewModel.articleIds.getOrNull(page + 1)]?.article?.title,
                onPrevious = { coroutineScope.launch { pagerState.animateScrollToPage(page - 1) } },
                onNext = { coroutineScope.launch { pagerState.animateScrollToPage(page + 1) } },
            )
        }
    }
}

@Composable
private fun ArticlePageContent(
    state: ArticleUiState,
    previousTitle: String?,
    nextTitle: String?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        state.error || state.article == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.home_error_generic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        else -> {
            val article = state.article
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
                val segments = remember(article.contentHtml) { parseArticleContent(article.contentHtml) }
                segments.forEach { segment ->
                    when (segment) {
                        is ContentSegment.Html -> ArticleWebView(
                            html = segment.html,
                            textColor = textColor,
                            linkColor = linkColor,
                            bodyFontSizeSp = bodyFontSizePx,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        is ContentSegment.YouTube -> YouTubeEmbedCard(
                            videoId = segment.videoId,
                            caption = segment.caption,
                            aspectRatio = segment.aspectRatio,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                ArticleNavFooter(
                    previousTitle = previousTitle,
                    nextTitle = nextTitle,
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }
        }
    }
}

@Composable
private fun ArticleNavFooter(
    previousTitle: String?,
    nextTitle: String?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    if (previousTitle == null && nextTitle == null) return

    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) {
            when {
                previousTitle != null && nextTitle != null -> {
                    NavFooterBlock(
                        label = stringResource(R.string.article_nav_previous),
                        title = previousTitle,
                        isNext = false,
                        onClick = onPrevious,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(16.dp))
                    NavFooterBlock(
                        label = stringResource(R.string.article_nav_next),
                        title = nextTitle,
                        isNext = true,
                        onClick = onNext,
                        modifier = Modifier.weight(1f),
                    )
                }
                previousTitle != null -> NavFooterBlock(
                    label = stringResource(R.string.article_nav_previous),
                    title = previousTitle,
                    isNext = false,
                    onClick = onPrevious,
                    modifier = Modifier.fillMaxWidth(),
                )
                nextTitle != null -> NavFooterBlock(
                    label = stringResource(R.string.article_nav_next),
                    title = nextTitle,
                    isNext = true,
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun NavFooterBlock(
    label: String,
    title: String,
    isNext: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        horizontalArrangement = if (isNext) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isNext) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Column(horizontalAlignment = if (isNext) Alignment.End else Alignment.Start) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (isNext) TextAlign.End else TextAlign.Start,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (isNext) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
