package bg.tochka.reader

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import bg.tochka.reader.notifications.NotificationConstants
import bg.tochka.reader.notifications.NotificationWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class TochkaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        schedulePeriodicNotificationCheck()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService<NotificationManager>() ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                NotificationConstants.CHANNEL_NEW_ARTICLES,
                getString(R.string.notif_channel_articles_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = getString(R.string.notif_channel_articles_desc) },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                NotificationConstants.CHANNEL_THREE_MINUTES,
                getString(R.string.notif_channel_three_minutes_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = getString(R.string.notif_channel_three_minutes_desc) },
        )
    }

    private fun schedulePeriodicNotificationCheck() {
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(30, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        // UPDATE (not KEEP) so that changes to the work request — like the network constraint
        // added here — actually reach devices that already had the job scheduled from an
        // earlier app version, instead of being silently ignored forever.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NotificationConstants.PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
