package bg.tochka.reader.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import bg.tochka.reader.R
import coil.compose.AsyncImage
import coil.request.ImageRequest

private val PORTRAIT_HEIGHT_CAP = 360.dp

/**
 * Renders a YouTube embed found in an article body as a native thumbnail + play button — never
 * inside the article's own scoped WebView. Tapping opens the YouTube app if installed, otherwise
 * a Chrome Custom Tab (falling back to a plain browser intent if even that fails).
 *
 * [aspectRatio] comes from the embed's own declared width/height — this site embeds YouTube
 * Shorts (vertical, ~9:16) about as often as regular 16:9 video, so it's read per-embed rather
 * than assumed. Portrait embeds are height-capped so one Short doesn't dominate the whole screen.
 */
@Composable
fun YouTubeEmbedCard(videoId: String, caption: String?, aspectRatio: Float, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var thumbnailUrl by remember(videoId) {
        mutableStateOf("https://img.youtube.com/vi/$videoId/maxresdefault.jpg")
    }
    val isPortrait = aspectRatio < 1f

    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = if (isPortrait) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        val boxModifier = if (isPortrait) {
            Modifier.height(PORTRAIT_HEIGHT_CAP).aspectRatio(aspectRatio)
        } else {
            Modifier.fillMaxWidth().aspectRatio(aspectRatio)
        }
        Box(
            modifier = boxModifier
                .clip(RoundedCornerShape(2.dp))
                .clickable { openYouTubeVideo(context, videoId) },
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .listener(onError = { _, _ ->
                        // maxresdefault isn't generated for every video; hqdefault always is.
                        if (thumbnailUrl.contains("maxresdefault")) {
                            thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                        }
                    })
                    .build(),
                contentDescription = caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.88f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.article_youtube_play_cd),
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
        if (caption != null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

private fun openYouTubeVideo(context: Context, videoId: String) {
    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId")).apply {
        setPackage("com.google.android.youtube")
    }
    val youTubeAppAvailable = appIntent.resolveActivity(context.packageManager) != null
    if (youTubeAppAvailable) {
        context.startActivity(appIntent)
        return
    }

    val webUri = Uri.parse("https://www.youtube.com/watch?v=$videoId")
    runCatching {
        CustomTabsIntent.Builder().build().launchUrl(context, webUri)
    }.onFailure {
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}
