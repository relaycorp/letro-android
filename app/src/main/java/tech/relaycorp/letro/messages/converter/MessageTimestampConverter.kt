package tech.relaycorp.letro.messages.converter

import tech.relaycorp.letro.utils.ext.isCurrentYear
import tech.relaycorp.letro.utils.ext.isToday
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface MessageTimestampConverter {
    fun convertBrief(timestamp: LocalDateTime): String
    fun convertDetailed(timestamp: LocalDateTime): String
}

class MessageTimestampConverterImpl @Inject constructor() : MessageTimestampConverter {

    override fun convertBrief(timestamp: LocalDateTime): String {
        return if (timestamp.isToday()) {
            timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"))
        } else if (timestamp.isCurrentYear()) {
            timestamp.format(DateTimeFormatter.ofPattern("dd MMM"))
        } else {
            timestamp.format(DateTimeFormatter.ofPattern("dd MMM y"))
        }
    }

    override fun convertDetailed(timestamp: LocalDateTime): String {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM y, hh:mm a"))
    }
}
