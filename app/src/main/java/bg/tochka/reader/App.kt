package bg.tochka.reader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Denied is fine — NotificationWorker checks areNotificationsEnabled() before posting. */ }

    // The new-articles toggle defaults to enabled, so the user never has to flip a switch to
    // turn notifications "on" — meaning the Settings-screen permission request (tied to
    // onCheckedChange) would never fire for a fresh install. Request it here once instead,
    // right after the first-launch disclaimer is out of the way.
    LaunchedEffect(settings?.disclaimerSeen) {
        val s = settings ?: return@LaunchedEffect
        if (!s.disclaimerSeen) return@LaunchedEffect
        if (!(s.newArticlesNotifEnabled || s.threeMinutesNotifEnabled)) return@LaunchedEffect
        val alreadyGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!alreadyGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
