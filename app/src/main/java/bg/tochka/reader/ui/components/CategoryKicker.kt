package bg.tochka.reader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** The small "● Разследвания" eyebrow label used above headlines throughout the app. */
@Composable
fun CategoryKicker(text: String, modifier: Modifier = Modifier, dotSize: androidx.compose.ui.unit.Dp = 6.dp) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
        Box(modifier = Modifier.width(6.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
