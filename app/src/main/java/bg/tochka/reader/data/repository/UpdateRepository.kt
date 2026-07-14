package bg.tochka.reader.data.repository

import bg.tochka.reader.BuildConfig
import bg.tochka.reader.data.remote.GithubApiService
import bg.tochka.reader.data.settings.SettingsRepository
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val versionTag: String,
    val downloadUrl: String,
)

@Singleton
class UpdateRepository @Inject constructor(
    private val githubApi: GithubApiService,
    private val settingsRepository: SettingsRepository,
) {
    private var checkedThisProcess = false

    /**
     * Checks GitHub Releases for a newer version than [BuildConfig.VERSION_NAME].
     *
     * Throttled to once per process launch and at most once every 24h (via the persisted
     * last-check timestamp), unless [force] is set (manual "check now" from Settings).
     */
    suspend fun checkForUpdate(force: Boolean = false): UpdateInfo? {
        if (!force) {
            if (checkedThisProcess) return null
            val lastCheck = settingsRepository.getLastUpdateCheckMillis()
            if (lastCheck != null && System.currentTimeMillis() - lastCheck < THROTTLE_MILLIS) {
                return null
            }
        }
        checkedThisProcess = true
        settingsRepository.setLastUpdateCheckMillis(System.currentTimeMillis())

        return try {
            val release = githubApi.getLatestRelease()
            val remoteVersion = release.tagName.removePrefix("v")
            if (remoteVersion.isBlank() || !isNewer(remoteVersion, BuildConfig.VERSION_NAME)) return null

            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                ?: return null

            UpdateInfo(versionTag = release.tagName, downloadUrl = apkAsset.browserDownloadUrl)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private fun isNewer(remoteVersion: String, localVersion: String): Boolean {
        val remoteParts = remoteVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val localParts = localVersion.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(remoteParts.size, localParts.size)) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r != l) return r > l
        }
        return false
    }

    companion object {
        private val THROTTLE_MILLIS = TimeUnit.HOURS.toMillis(24)
    }
}
