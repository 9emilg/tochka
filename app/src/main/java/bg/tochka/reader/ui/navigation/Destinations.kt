package bg.tochka.reader.ui.navigation

object Destinations {
    const val HOME = "home"
    const val SAVED = "saved"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val ABOUT = "about"
    const val ARTICLE = "article/{articleId}"
    const val ARTICLE_ID_ARG = "articleId"

    fun article(articleId: Int): String = "article/$articleId"
}
