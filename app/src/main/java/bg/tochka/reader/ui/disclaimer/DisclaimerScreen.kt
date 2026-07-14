package bg.tochka.reader.ui.disclaimer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import bg.tochka.reader.R

/** The dot + title + body + dismiss button shared by the full-screen About page and the popup. */
@Composable
fun DisclaimerContent(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
        Text(
            text = stringResource(R.string.disclaimer_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 18.dp),
        )
        Text(
            text = stringResource(R.string.disclaimer_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp),
        )
        Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(2.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp),
        ) {
            Text(
                text = stringResource(R.string.disclaimer_dismiss),
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }
}

/** Full-screen variant, used for the "За приложението" row in Settings — reachable any time. */
@Composable
fun DisclaimerScreen(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        DisclaimerContent(
            onDismiss = onDismiss,
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
        )
    }
}

/**
 * First-launch variant: a centered card over a scrim + blurred Home screen, rather than a
 * full-screen takeover — the app underneath is already loaded and visible (just softened).
 */
@Composable
fun DisclaimerPopup(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            // Consume taps so they can't reach the (blurred) screen underneath — this is a
            // blocking modal, not a dismissible scrim.
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            DisclaimerContent(onDismiss = onDismiss)
        }
    }
}
