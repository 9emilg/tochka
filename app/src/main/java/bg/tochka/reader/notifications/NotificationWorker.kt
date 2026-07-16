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

private const val MAX_INBOX_LINES = 5
private const val SUMMARY_NOTIFICATION_ID_GENERAL = -1001
private const val SUMMARY_NOTIFICATION_ID_THREE_MINUTES = -1002

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
                summaryNotificationId = SUMMARY_NOTIFICATION_ID_GENERAL,
                groupTitleRes = R.string.notif_grouped_title_general,
                getLastSeen = settingsRepository::getLastSeenGeneralPostId,
                setLastSeen = settingsRepository::setLastSeenGeneralPostId,
            )
        }
        if (settings.threeMinutesNotifEnabled) {
            checkAndNotify(
                categorySlug = CategoryNames.THREE_MINUTES_SLUG,
                excludeCategoryDisplayName = null,
                channelId = NotificationConstants.CHANNEL_THREE_MINUTES,
                summaryNotificationId = SUMMARY_NOTIFICATION_ID_THREE_MINUTES,
                groupTitleRes = R.string.notif_grouped_title_three_minutes,
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
        summaryNotificationId: Int,
        groupTitleRes: Int,
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

        // A single new article gets its own notification; several at once (e.g. after a period
        // where the background check was deferred by the OS) are grouped into one summary
        // notification instead of posting one per article.
        if (newPosts.size == 1) {
            postSingleNotification(channelId, newPosts.first())
        } else {
            postGroupedNotification(channelId, summaryNotificationId, groupTitleRes, newPosts)
        }
        setLastSeen(newestId)
    }

    private fun postSingleNotification(channelId: String, article: Article) {
        val context = applicationContext
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(article.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(article.title))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent(article.id))
            .build()

        runCatching { manager.notify(article.id, notification) }
    }

    private fun postGroupedNotification(
        channelId: String,
        summaryNotificationId: Int,
        groupTitleRes: Int,
        articles: List<Article>,
    ) {
        val context = applicationContext
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val title = context.getString(groupTitleRes, articles.size)
        val shown = articles.takeLast(MAX_INBOX_LINES).asReversed()
        val overflow = articles.size - shown.size

        val inbox = NotificationCompat.InboxStyle().setBigContentTitle(title)
        shown.forEach { inbox.addLine(it.title) }
        if (overflow > 0) {
            inbox.setSummaryText(context.getString(R.string.notif_grouped_more, overflow))
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setStyle(inbox)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent(summaryNotificationId))
            .build()

        runCatching { manager.notify(summaryNotificationId, notification) }
    }

    private fun openAppPendingIntent(requestCode: Int): PendingIntent {
        val context = applicationContext
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
