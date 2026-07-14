package bg.tochka.reader.data.settings

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

enum class FontSize(val scale: Float) {
    SMALL(0.9f),
    MEDIUM(1.0f),
    LARGE(1.15f),
}
