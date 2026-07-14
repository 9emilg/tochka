package bg.tochka.reader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bg.tochka.reader.domain.model.Article

@Composable
fun FeaturedArticleCard(
    article: Article,
    metaLine: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kickerOverride: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 16.dp, bottom = 4.dp),
    ) {
        val kicker = kickerOverride ?: article.primaryCategory
        kicker?.let { CategoryKicker(text = it) }
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 9.dp, bottom = 12.dp),
        )
        ArticleThumbnail(
            imageUrl = article.imageUrl,
            contentDescription = article.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(186.dp),
        )
        Row(modifier = Modifier.padding(top = 10.dp)) {
            Text(
                text = metaLine,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
