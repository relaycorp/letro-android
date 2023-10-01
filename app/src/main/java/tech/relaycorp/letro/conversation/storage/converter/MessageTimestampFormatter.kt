package tech.relaycorp.letro.conversation.storage.converter

import tech.relaycorp.letro.utils.ext.isCurrentYear
import tech.relaycorp.letro.utils.ext.isToday
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface MessageTimestampFormatter {
    fun formatBrief(timestamp: LocalDateTime): String
    fun formatDetailed(timestamp: LocalDateTime): String
}

class MessageTimestampFormatterImpl @Inject constructor() : MessageTimestampFormatter {

    override fun formatBrief(timestamp: LocalDateTime): String {
        return if (timestamp.isToday()) {
            timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"))
        } else if (timestamp.isCurrentYear()) {
            timestamp.format(DateTimeFormatter.ofPattern("dd MMM"))
        } else {
            timestamp.format(DateTimeFormatter.ofPattern("dd MMM y"))
        }
    }

    override fun formatDetailed(timestamp: LocalDateTime): String {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM y, hh:mm a"))
    }
}
