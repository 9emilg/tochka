package bg.tochka.reader.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.tochka.reader.data.repository.UpdateInfo
import bg.tochka.reader.data.repository.UpdateRepository
import bg.tochka.reader.data.settings.SettingsRepository
import bg.tochka.reader.update.DownloadState
import bg.tochka.reader.update.UpdateInstaller
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class ManualCheckResult { NONE, CHECKING, UP_TO_DATE, UPDATE_FOUND }

data class UpdateUiState(
    val availableUpdate: UpdateInfo? = null,
    val downloadState: DownloadState = DownloadState.IDLE,
    val manualCheckResult: ManualCheckResult = ManualCheckResult.NONE,
)

/**
 * Shared across Home (banner) and Settings (manual check + toggle) — hoisted once at the
 * NavHost level so both screens see the same in-flight/dismissed state.
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val settingsRepository: SettingsRepository,
    private val updateInstaller: UpdateInstaller,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            updateInstaller.state.collect { downloadState ->
                _uiState.update { it.copy(downloadState = downloadState) }
            }
        }
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            if (!settings.autoUpdateCheckEnabled) return@launch
            checkInternal(force = false)
        }
    }

    fun checkManually() {
        viewModelScope.launch {
            _uiState.update { it.copy(manualCheckResult = ManualCheckResult.CHECKING) }
            checkInternal(force = true)
            val found = _uiState.value.availableUpdate != null
            _uiState.update {
                it.copy(manualCheckResult = if (found) ManualCheckResult.UPDATE_FOUND else ManualCheckResult.UP_TO_DATE)
            }
        }
    }

    private suspend fun checkInternal(force: Boolean) {
        val info = withContext(Dispatchers.IO) { updateRepository.checkForUpdate(force = force) }
        if (info == null) return
        val dismissedTag = settingsRepository.getDismissedUpdateTag()
        if (info.versionTag != dismissedTag) {
            _uiState.update { it.copy(availableUpdate = info) }
        }
    }

    fun dismissBanner() {
        val info = _uiState.value.availableUpdate ?: return
        viewModelScope.launch { settingsRepository.setDismissedUpdateTag(info.versionTag) }
        _uiState.update { it.copy(availableUpdate = null) }
    }

    fun startUpdate() {
        val info = _uiState.value.availableUpdate ?: return
        updateInstaller.startDownload(info.downloadUrl)
    }

    fun setAutoCheckEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoUpdateCheckEnabled(enabled) }
    }
}
