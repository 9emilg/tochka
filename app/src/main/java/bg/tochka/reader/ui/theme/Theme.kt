package bg.tochka.reader.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import bg.tochka.reader.data.settings.FontSize
import bg.tochka.reader.data.settings.ThemeMode

data class TochkaExtendedColors(
    val hairline: Color,
    val muted: Color,
    val accentPressed: Color,
)

private val LightExtendedColors = TochkaExtendedColors(
    hairline = LightHairline,
    muted = LightMuted,
    accentPressed = AccentPressedLight,
)

private val DarkExtendedColors = TochkaExtendedColors(
    hairline = DarkHairline,
    muted = DarkMuted,
    accentPressed = AccentPressedDark,
)

val LocalTochkaColors = staticCompositionLocalOf { LightExtendedColors }
val LocalTochkaFontScale = staticCompositionLocalOf { FontSize.MEDIUM.scale }

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightMuted,
    outline = LightDivider,
    outlineVariant = LightHairline,
)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkMuted,
    outline = DarkDivider,
    outlineVariant = DarkHairline,
)

@Composable
fun TochkaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    fontSize: FontSize = FontSize.MEDIUM,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDarkTheme) DarkColors else LightColors
    val extendedColors = if (useDarkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = LocalContext.current as? Activity
        SideEffect {
            activity?.window?.let { window ->
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !useDarkTheme
                controller.isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalTochkaColors provides extendedColors,
        LocalTochkaFontScale provides fontSize.scale,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = tochkaTypography(fontSize.scale),
            content = content,
        )
    }
}
