package bg.tochka.reader.ui.article

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import bg.tochka.reader.data.remote.WpApiService

/**
 * Renders `content.rendered` in a WebView scoped to just the article body — not a full
 * embedded browser — so inline links, images and embeds render with the app's typography.
 *
 * The page reports its own rendered height back to Kotlin via a JS interface, driven by a
 * ResizeObserver (not a one-shot measurement on page-finished, which runs before images have
 * loaded and reflowed — that undershoots the true height and cuts articles off early) so it can
 * sit inline inside a scrolling Compose column instead of scrolling internally.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArticleWebView(
    html: String,
    textColor: Color,
    linkColor: Color,
    bodyFontSizeSp: Float,
    modifier: Modifier = Modifier,
) {
    var contentHeightDp by remember { mutableFloatStateOf(0f) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    val wrapped = remember(html, textColor, linkColor, bodyFontSizeSp) {
        wrapArticleHtml(html, textColor, linkColor, bodyFontSizeSp)
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(if (contentHeightDp > 0f) contentHeightDp.dp else 1.dp),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setBackgroundColor(AndroidColor.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = false
                isVerticalScrollBarEnabled = false
                isNestedScrollingEnabled = false
                addJavascriptInterface(
                    object {
                        // `document.documentElement.scrollHeight` is reported in CSS px, which —
                        // under our `width=device-width, initial-scale=1` viewport — is already
                        // numerically equal to Android dp. Do NOT run this through Density.toDp()
                        // (that divides by density again and undershoots on anything but 1x
                        // screens, which is why articles were getting cut off before reaching
                        // the true end).
                        @JavascriptInterface
                        fun reportHeight(heightDp: Float) {
                            mainHandler.post {
                                // Small safety margin: rounding/late reflow should never re-clip content.
                                val padded = heightDp + 8f
                                if (padded > contentHeightDp) contentHeightDp = padded
                            }
                        }
                    },
                    "AndroidHeight",
                )
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                WpApiService.BASE_URL,
                wrapped,
                "text/html",
                "UTF-8",
                null,
            )
        },
    )
}

private fun Color.toCssHex(): String {
    val argb = this.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}

private fun wrapArticleHtml(
    bodyHtml: String,
    textColor: Color,
    linkColor: Color,
    bodyFontSizeSp: Float,
): String = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
      <style>
        html, body {
          margin: 0;
          padding: 0;
          background: transparent;
          color: ${textColor.toCssHex()};
          font-family: sans-serif;
          font-size: ${bodyFontSizeSp}px;
          line-height: 1.72;
        }
        img, iframe, video { max-width: 100%; height: auto; border-radius: 2px; }
        a { color: ${linkColor.toCssHex()}; }
        p { margin: 0 0 16px; }
        blockquote {
          margin: 0 0 16px;
          padding-left: 16px;
          border-left: 3px solid ${linkColor.toCssHex()};
          opacity: 0.85;
        }
        h1, h2, h3, h4 { line-height: 1.25; }
        figure { margin: 0 0 16px; }
        figcaption { font-size: 0.8em; opacity: 0.7; }
      </style>
    </head>
    <body>
    $bodyHtml
    <script>
      function reportHeight() {
        var h = document.documentElement.scrollHeight;
        if (window.AndroidHeight) window.AndroidHeight.reportHeight(h);
      }
      reportHeight();
      window.addEventListener('load', reportHeight);
      document.querySelectorAll('img').forEach(function (img) {
        img.addEventListener('load', reportHeight);
        img.addEventListener('error', reportHeight);
      });
      if (window.ResizeObserver) {
        new ResizeObserver(reportHeight).observe(document.body);
      } else {
        setTimeout(reportHeight, 300);
        setTimeout(reportHeight, 1000);
        setTimeout(reportHeight, 2500);
      }
    </script>
    </body>
    </html>
""".trimIndent()
