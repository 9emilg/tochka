package bg.tochka.reader.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontSize: FontSize = FontSize.MEDIUM,
    /** New-article headline notifications, excluding the "Три минути" category. */
    val newArticlesNotifEnabled: Boolean = true,
    /** Notifications specifically for new "Три минути" posts. */
    val threeMinutesNotifEnabled: Boolean = false,
    val disclaimerSeen: Boolean = false,
)

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val NEW_ARTICLES_NOTIF = booleanPreferencesKey("notif_new_articles")
        val THREE_MINUTES_NOTIF = booleanPreferencesKey("notif_three_minutes")
        val DISCLAIMER_SEEN = booleanPreferencesKey("disclaimer_seen")
        val LAST_SEEN_GENERAL_POST_ID = intPreferencesKey("last_seen_general_post_id")
        val LAST_SEEN_THREE_MINUTES_POST_ID = intPreferencesKey("last_seen_three_minutes_post_id")
    }

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            fontSize = prefs[Keys.FONT_SIZE]?.let { runCatching { FontSize.valueOf(it) }.getOrNull() }
                ?: FontSize.MEDIUM,
            newArticlesNotifEnabled = prefs[Keys.NEW_ARTICLES_NOTIF] ?: true,
            threeMinutesNotifEnabled = prefs[Keys.THREE_MINUTES_NOTIF] ?: false,
            disclaimerSeen = prefs[Keys.DISCLAIMER_SEEN] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setFontSize(size: FontSize) {
        dataStore.edit { it[Keys.FONT_SIZE] = size.name }
    }

    suspend fun setNewArticlesNotifEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NEW_ARTICLES_NOTIF] = enabled }
    }

    suspend fun setThreeMinutesNotifEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.THREE_MINUTES_NOTIF] = enabled }
    }

    suspend fun setDisclaimerSeen(seen: Boolean) {
        dataStore.edit { it[Keys.DISCLAIMER_SEEN] = seen }
    }

    suspend fun getLastSeenGeneralPostId(): Int? = dataStore.data.first()[Keys.LAST_SEEN_GENERAL_POST_ID]

    suspend fun setLastSeenGeneralPostId(id: Int) {
        dataStore.edit { it[Keys.LAST_SEEN_GENERAL_POST_ID] = id }
    }

    suspend fun getLastSeenThreeMinutesPostId(): Int? = dataStore.data.first()[Keys.LAST_SEEN_THREE_MINUTES_POST_ID]

    suspend fun setLastSeenThreeMinutesPostId(id: Int) {
        dataStore.edit { it[Keys.LAST_SEEN_THREE_MINUTES_POST_ID] = id }
    }
}
