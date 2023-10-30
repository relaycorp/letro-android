package tech.relaycorp.letro.conversation.storage.converter

import tech.relaycorp.letro.utils.time.isCurrentYear
import tech.relaycorp.letro.utils.time.isToday
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface MessageTimestampFormatter {
    fun formatBrief(timestamp: ZonedDateTime): String
    fun formatDetailed(timestamp: ZonedDateTime): String
}

class MessageTimestampFormatterImpl @Inject constructor() : MessageTimestampFormatter {

    override fun formatBrief(timestamp: ZonedDateTime): String {
        return if (timestamp.isToday()) {
            timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"))
        } else if (timestamp.isCurrentYear()) {
            timestamp.format(DateTimeFormatter.ofPattern("dd MMM"))
        } else {
            timestamp.format(DateTimeFormatter.ofPattern("dd MMM y"))
        }
    }

    override fun formatDetailed(timestamp: ZonedDateTime): String {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM y, hh:mm a"))
    }
}
