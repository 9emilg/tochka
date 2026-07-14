package bg.tochka.reader.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

enum class DownloadState { IDLE, DOWNLOADING, INSTALL_READY, FAILED }

private const val APK_FILE_NAME = "tochka-update.apk"

/**
 * Downloads a release APK via [DownloadManager] into app-external-files storage (no storage
 * permission needed) and, once complete, hands it to the system package installer through a
 * [FileProvider] content:// URI.
 */
@Singleton
class UpdateInstaller @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val _state = MutableStateFlow(DownloadState.IDLE)
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    private var pendingDownloadId: Long = -1L

    private val downloadManager: DownloadManager?
        get() = ContextCompat.getSystemService(context, DownloadManager::class.java)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id == -1L || id != pendingDownloadId) return
            onDownloadFinished()
        }
    }

    init {
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    fun startDownload(apkUrl: String) {
        val manager = downloadManager ?: run { _state.value = DownloadState.FAILED; return }

        // Always overwrite the same file so downloads never pile up.
        apkFile.delete()

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("Точка — актуализация")
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, APK_FILE_NAME)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setMimeType("application/vnd.android.package-archive")

        pendingDownloadId = manager.enqueue(request)
        _state.value = DownloadState.DOWNLOADING
    }

    private fun onDownloadFinished() {
        if (!apkFile.exists()) {
            _state.value = DownloadState.FAILED
            return
        }
        _state.value = DownloadState.INSTALL_READY
        launchInstaller()
    }

    private fun launchInstaller() {
        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { context.startActivity(installIntent) }
            .onFailure { _state.value = DownloadState.FAILED }
    }

    private val apkFile: File
        get() = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME)
}
