package bg.tochka.reader.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import bg.tochka.reader.MainActivity
import bg.tochka.reader.R
import bg.tochka.reader.data.repository.ArticleRepository
import bg.tochka.reader.data.settings.SettingsRepository
import bg.tochka.reader.domain.model.Article
import bg.tochka.reader.domain.model.CategoryNames
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

private const val MAX_NOTIFICATIONS_PER_RUN = 5

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = settingsRepository.settings.first()
        val threeMinutesName = CategoryNames.displayName(
            CategoryNames.THREE_MINUTES_SLUG,
            CategoryNames.THREE_MINUTES_SLUG,
        )

        if (settings.newArticlesNotifEnabled) {
            checkAndNotify(
                categorySlug = null,
                excludeCategoryDisplayName = threeMinutesName,
                channelId = NotificationConstants.CHANNEL_NEW_ARTICLES,
                getLastSeen = settingsRepository::getLastSeenGeneralPostId,
                setLastSeen = settingsRepository::setLastSeenGeneralPostId,
            )
        }
        if (settings.threeMinutesNotifEnabled) {
            checkAndNotify(
                categorySlug = CategoryNames.THREE_MINUTES_SLUG,
                excludeCategoryDisplayName = null,
                channelId = NotificationConstants.CHANNEL_THREE_MINUTES,
                getLastSeen = settingsRepository::getLastSeenThreeMinutesPostId,
                setLastSeen = settingsRepository::setLastSeenThreeMinutesPostId,
            )
        }
        return Result.success()
    }

    private suspend fun checkAndNotify(
        categorySlug: String?,
        excludeCategoryDisplayName: String?,
        channelId: String,
        getLastSeen: suspend () -> Int?,
        setLastSeen: suspend (Int) -> Unit,
    ) {
        val posts = runCatching {
            articleRepository.getPosts(categorySlug = categorySlug, page = 1, perPage = 20)
        }.getOrNull() ?: return

        val filtered = if (excludeCategoryDisplayName != null) {
            posts.filterNot { it.categories.contains(excludeCategoryDisplayName) }
        } else {
            posts
        }
        if (filtered.isEmpty()) return

        val lastSeenId = getLastSeen()
        val newestId = filtered.maxOf { it.id }

        if (lastSeenId == null) {
            // First run: establish a baseline, don't notify for the whole existing backlog.
            setLastSeen(newestId)
            return
        }

        val newPosts = filtered.filter { it.id > lastSeenId }.sortedBy { it.id }
        if (newPosts.isEmpty()) return

        newPosts.takeLast(MAX_NOTIFICATIONS_PER_RUN).forEach { article ->
            postNotification(channelId, article)
        }
        setLastSeen(newestId)
    }

    private fun postNotification(channelId: String, article: Article) {
        val context = applicationContext
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            article.id,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(article.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(article.title))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching { manager.notify(article.id, notification) }
    }
}
