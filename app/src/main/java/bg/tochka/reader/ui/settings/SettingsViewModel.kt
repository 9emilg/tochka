package bg.tochka.reader.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.tochka.reader.data.settings.AppSettings
import bg.tochka.reader.data.settings.FontSize
import bg.tochka.reader.data.settings.SettingsRepository
import bg.tochka.reader.data.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setFontSize(size: FontSize) {
        viewModelScope.launch { settingsRepository.setFontSize(size) }
    }

    fun setNewArticlesNotifEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNewArticlesNotifEnabled(enabled) }
    }

    fun setThreeMinutesNotifEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setThreeMinutesNotifEnabled(enabled) }
    }

    fun setDisclaimerSeen(seen: Boolean) {
        viewModelScope.launch { settingsRepository.setDisclaimerSeen(seen) }
    }
}
