package bg.tochka.reader.ui.navigation

object Destinations {
    const val HOME = "home"
    const val SAVED = "saved"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val ABOUT = "about"
    const val ARTICLE = "article/{articleId}/{articleIds}"
    const val ARTICLE_ID_ARG = "articleId"
    const val ARTICLE_IDS_ARG = "articleIds"

    /** [articleIds] is the ordered list the user was browsing (its own feed tab, Saved, or
     * search results) — the article screen swipes within this exact list, in this exact order. */
    fun article(articleId: Int, articleIds: List<Int>): String =
        "article/$articleId/${articleIds.joinToString(",")}"
}
