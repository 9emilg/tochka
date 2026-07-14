package bg.tochka.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bg.tochka.reader.data.settings.AppSettings
import bg.tochka.reader.ui.disclaimer.DisclaimerPopup
import bg.tochka.reader.ui.navigation.TochkaNavHost
import bg.tochka.reader.ui.settings.SettingsViewModel
import bg.tochka.reader.ui.theme.TochkaTheme

@Composable
fun TochkaApp() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings = settingsViewModel.settings.collectAsState().value

    // Settings haven't loaded from DataStore yet — render an empty themed surface for one
    // frame rather than flashing the first-launch disclaimer for returning users.
    val resolved = settings ?: AppSettings()
    val showDisclaimer = settings != null && !settings.disclaimerSeen

    TochkaTheme(themeMode = resolved.themeMode, fontSize = resolved.fontSize) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = if (showDisclaimer) Modifier.blur(20.dp) else Modifier) {
                    TochkaNavHost()
                }
                if (showDisclaimer) {
                    DisclaimerPopup(onDismiss = { settingsViewModel.setDisclaimerSeen(true) })
                }
            }
        }
    }
}
