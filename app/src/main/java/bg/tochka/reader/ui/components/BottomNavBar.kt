package bg.tochka.reader.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bg.tochka.reader.R
import bg.tochka.reader.ui.navigation.Destinations

private data class NavItem(
    val route: String,
    val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navItems = listOf(
    NavItem(Destinations.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(Destinations.SAVED, R.string.nav_saved, Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    NavItem(Destinations.SETTINGS, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun TochkaBottomNavBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 9.dp, bottom = 10.dp),
        ) {
            navItems.forEach { item ->
                val selected = currentRoute == item.route
                val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                val label = stringResource(item.label)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .selectable(selected = selected, onClick = { onNavigate(item.route) }),
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = color,
                    )
                }
            }
        }
    }
}
