package tech.relaycorp.letro.notification.converter

import androidx.annotation.StringRes
import tech.relaycorp.letro.R
import tech.relaycorp.letro.utils.ext.isLessThanDayAgo
import tech.relaycorp.letro.utils.ext.isLessThanHourAgo
import tech.relaycorp.letro.utils.ext.isLessThanWeekAgo
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

interface ExtendedNotificationDateFormatter {

    fun format(timestamp: LocalDateTime): NotificationDateInfo
}

class ExtendedNotificationDateFormatterImpl @Inject constructor() : ExtendedNotificationDateFormatter {

    override fun format(timestamp: LocalDateTime): NotificationDateInfo {
        return when {
            timestamp.isLessThanHourAgo() -> NotificationDateInfo(
                value = ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now()),
                stringRes = R.string.notification_time_minutes,
                timestamp = timestamp,
            )
            timestamp.isLessThanDayAgo() -> NotificationDateInfo(
                value = ChronoUnit.HOURS.between(timestamp, LocalDateTime.now()),
                stringRes = R.string.notification_time_hours,
                timestamp = timestamp,
            )
            timestamp.isLessThanWeekAgo() -> NotificationDateInfo(
                value = ChronoUnit.DAYS.between(timestamp, LocalDateTime.now()),
                stringRes = R.string.notification_time_days,
                timestamp = timestamp,
            )
            else -> NotificationDateInfo(
                value = ChronoUnit.WEEKS.between(timestamp, LocalDateTime.now()),
                stringRes = R.string.notification_time_weeks,
                timestamp = timestamp,
            )
        }
    }
}

data class NotificationDateInfo(
    val value: Long,
    @StringRes val stringRes: Int,
    val timestamp: LocalDateTime,
)
