package tech.relaycorp.letro.utils.ext

import java.time.LocalDateTime

fun LocalDateTime.isToday(): Boolean {
    val now = LocalDateTime.now()
    return now.dayOfMonth == this.dayOfMonth && now.month == this.month && now.year == this.year
}

fun LocalDateTime.isCurrentYear(): Boolean {
    val now = LocalDateTime.now()
    return now.year == this.year
}

fun LocalDateTime.isLessThanHourAgo(): Boolean {
    val now = LocalDateTime.now()
    return now.minusHours(1L) <= this
}

fun LocalDateTime.isLessThanDayAgo(): Boolean {
    val now = LocalDateTime.now()
    return now.minusDays(1L) <= this
}

fun LocalDateTime.isLessThanWeekAgo(): Boolean {
    val now = LocalDateTime.now()
    return now.minusWeeks(1L) <= this
}
