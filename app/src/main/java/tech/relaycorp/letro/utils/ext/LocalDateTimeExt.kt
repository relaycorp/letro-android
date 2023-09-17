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
