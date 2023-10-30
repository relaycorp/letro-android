package tech.relaycorp.letro.notification.converter

import androidx.annotation.StringRes
import tech.relaycorp.letro.R
import tech.relaycorp.letro.utils.time.isLessThanDayAgo
import tech.relaycorp.letro.utils.time.isLessThanHourAgo
import tech.relaycorp.letro.utils.time.isLessThanWeeksAgo
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

interface ExtendedNotificationDateFormatter {

    fun format(timestamp: ZonedDateTime): NotificationDateInfo
}

class ExtendedNotificationDateFormatterImpl @Inject constructor() : ExtendedNotificationDateFormatter {

    override fun format(timestamp: ZonedDateTime): NotificationDateInfo {
        return when {
            timestamp.isLessThanHourAgo() -> NotificationDateInfo(
                value = ChronoUnit.MINUTES.between(timestamp, ZonedDateTime.now()),
                stringRes = R.string.notification_time_minutes,
                timestamp = timestamp,
            )
            timestamp.isLessThanDayAgo() -> NotificationDateInfo(
                value = ChronoUnit.HOURS.between(timestamp, ZonedDateTime.now()),
                stringRes = R.string.notification_time_hours,
                timestamp = timestamp,
            )
            timestamp.isLessThanWeeksAgo(1L) -> NotificationDateInfo(
                value = ChronoUnit.DAYS.between(timestamp, ZonedDateTime.now()),
                stringRes = R.string.notification_time_days,
                timestamp = timestamp,
            )
            else -> NotificationDateInfo(
                value = ChronoUnit.WEEKS.between(timestamp, ZonedDateTime.now()),
                stringRes = R.string.notification_time_weeks,
                timestamp = timestamp,
            )
        }
    }
}

data class NotificationDateInfo(
    val value: Long,
    @StringRes val stringRes: Int,
    val timestamp: ZonedDateTime,
)
