package bg.tochka.reader.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import bg.tochka.reader.BuildConfig
import bg.tochka.reader.R
import bg.tochka.reader.data.settings.FontSize
import bg.tochka.reader.data.settings.ThemeMode
import bg.tochka.reader.ui.components.SegmentedControl
import bg.tochka.reader.ui.theme.tochkaScreenTitleStyle

@Composable
fun SettingsScreen(
    onAboutClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings = viewModel.settings.collectAsState().value ?: return
    val context = LocalContext.current

    var pendingEnable by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) pendingEnable?.invoke(true)
        pendingEnable = null
    }

    fun setNotifToggle(enable: Boolean, setter: (Boolean) -> Unit) {
        val needsPermission = enable &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingEnable = setter
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            setter(enable)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 22.dp, bottom = 18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
            Text(
                text = stringResource(R.string.settings_title),
                style = tochkaScreenTitleStyle(settings.fontSize.scale),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        SectionHeader(stringResource(R.string.settings_section_appearance))

        Text(
            text = stringResource(R.string.settings_theme_label),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 9.dp),
        )
        SegmentedControl(
            options = listOf(
                ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
                ThemeMode.DARK to stringResource(R.string.settings_theme_dark),
                ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
            ),
            selected = settings.themeMode,
            onSelect = viewModel::setThemeMode,
            modifier = Modifier.padding(bottom = 22.dp),
        )

        Text(
            text = stringResource(R.string.settings_font_size_label),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 9.dp),
        )
        SegmentedControl(
            options = listOf(
                FontSize.SMALL to stringResource(R.string.settings_font_size_small),
                FontSize.MEDIUM to stringResource(R.string.settings_font_size_medium),
                FontSize.LARGE to stringResource(R.string.settings_font_size_large),
            ),
            selected = settings.fontSize,
            onSelect = viewModel::setFontSize,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        SectionHeader(stringResource(R.string.settings_section_notifications))

        SettingsToggleRow(
            label = stringResource(R.string.settings_notif_breaking),
            checked = settings.newArticlesNotifEnabled,
            onCheckedChange = { enable -> setNotifToggle(enable, viewModel::setNewArticlesNotifEnabled) },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SettingsToggleRow(
            label = stringResource(R.string.settings_notif_daily),
            checked = settings.threeMinutesNotifEnabled,
            onCheckedChange = { enable -> setNotifToggle(enable, viewModel::setThreeMinutesNotifEnabled) },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_language_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.settings_language_value),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAboutClick)
                .padding(vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_about_row),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = stringResource(R.string.settings_legal_notice, java.time.Year.now().value),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}
