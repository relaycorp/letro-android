package tech.relaycorp.letro.utils.time

import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

fun ZonedDateTime.isToday(): Boolean {
    val now = nowUTC()
    val utcTimeZoneThis = toUtcTimeZone()
    return now.dayOfMonth == utcTimeZoneThis.dayOfMonth && now.month == utcTimeZoneThis.month && now.year == utcTimeZoneThis.year
}

fun ZonedDateTime.isCurrentYear(): Boolean {
    val now = nowUTC()
    return now.year == this.toUtcTimeZone().year
}

fun ZonedDateTime.isLessThanHourAgo(): Boolean {
    val now = nowUTC()
    return now.minusHours(1L) <= this.toUtcTimeZone()
}

fun ZonedDateTime.isLessThanDayAgo(): Boolean {
    val now = nowUTC()
    return now.minusDays(1L) <= this.toUtcTimeZone()
}

fun ZonedDateTime.isLessThanWeeksAgo(weeks: Long): Boolean {
    val now = nowUTC()
    val utcTimeZoneThis = toUtcTimeZone()
    return now.minusWeeks(weeks) <= utcTimeZoneThis
}

fun ZonedDateTime.toSystemTimeZone(): ZonedDateTime {
    return this.withZoneSameInstant(ZoneId.systemDefault())
}

fun ZonedDateTime.toUtcTimeZone(): ZonedDateTime {
    return this.withZoneSameInstant(ZoneId.of(ZoneOffset.UTC.toString()))
}

fun nowUTC(): ZonedDateTime {
    return ZonedDateTime.now(ZoneId.of(ZoneOffset.UTC.toString()))
}
