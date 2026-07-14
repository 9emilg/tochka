package bg.tochka.reader.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import bg.tochka.reader.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private fun googleFont(name: String) = GoogleFont(name)

val NewsreaderFamily = FontFamily(
    Font(googleFont = googleFont("Newsreader"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = googleFont("Newsreader"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = googleFont("Newsreader"), fontProvider = fontProvider, weight = FontWeight.SemiBold),
)

val ArchivoFamily = FontFamily(
    Font(googleFont = googleFont("Archivo"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = googleFont("Archivo"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = googleFont("Archivo"), fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = googleFont("Archivo"), fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = googleFont("Archivo"), fontProvider = fontProvider, weight = FontWeight.ExtraBold),
)

/**
 * Builds the Material3 [Typography] for the given text-size multiplier
 * (Settings → Small/Medium/Large), scaling every sp value uniformly.
 */
fun tochkaTypography(scale: Float): Typography {
    fun sp(value: Int) = (value * scale).sp
    fun lh(value: Number) = (value.toFloat() * scale).sp

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = NewsreaderFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(30),
            lineHeight = lh(33),
            letterSpacing = (-0.012).em,
        ),
        headlineMedium = TextStyle(
            fontFamily = NewsreaderFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(27),
            lineHeight = lh(29),
            letterSpacing = (-0.01).em,
        ),
        headlineSmall = TextStyle(
            fontFamily = NewsreaderFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(24),
            lineHeight = lh(26),
            letterSpacing = (-0.01).em,
        ),
        titleLarge = TextStyle(
            fontFamily = NewsreaderFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(21),
            lineHeight = lh(26),
        ),
        titleMedium = TextStyle(
            fontFamily = NewsreaderFamily,
            fontWeight = FontWeight.Medium,
            fontSize = sp(17),
            lineHeight = lh(20),
        ),
        titleSmall = TextStyle(
            fontFamily = ArchivoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = sp(15),
            lineHeight = lh(20),
        ),
        bodyLarge = TextStyle(
            fontFamily = ArchivoFamily,
            fontWeight = FontWeight.Normal,
            fontSize = sp(16),
            lineHeight = lh(27.5),
        ),
        bodyMedium = TextStyle(
            fontFamily = ArchivoFamily,
            fontWeight = FontWeight.Normal,
            fontSize = sp(14),
            lineHeight = lh(21.7),
        ),
        bodySmall = TextStyle(
            fontFamily = ArchivoFamily,
            fontWeight = FontWeight.Normal,
            fontSize = sp(12),
            lineHeight = lh(16),
        ),
        labelLarge = TextStyle(
            fontFamily = ArchivoFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(13),
            lineHeight = lh(16),
        ),
        labelMedium = TextStyle(
            fontFamily = ArchivoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = sp(12),
            lineHeight = lh(15),
        ),
        labelSmall = TextStyle(
            fontFamily = ArchivoFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(11),
            lineHeight = lh(14),
            letterSpacing = 0.09.em,
        ),
    )
}

/** Extra-bold Archivo brand/section title (e.g. "Запазени", "Настройки") — heavier than M3's titleLarge role. */
fun tochkaScreenTitleStyle(scale: Float): TextStyle = TextStyle(
    fontFamily = ArchivoFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = (22 * scale).sp,
    letterSpacing = (-0.01).em,
)
