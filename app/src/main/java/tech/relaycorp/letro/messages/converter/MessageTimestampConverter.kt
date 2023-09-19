package tech.relaycorp.letro.messages.converter

import tech.relaycorp.letro.utils.ext.isCurrentYear
import tech.relaycorp.letro.utils.ext.isToday
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface MessageTimestampConverter {
    fun convert(timestamp: LocalDateTime): String
}

class MessageTimestampConverterImpl @Inject constructor() : MessageTimestampConverter {

    override fun convert(timestamp: LocalDateTime): String {
        return if (timestamp.isToday()) {
            timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"))
        } else if (timestamp.isCurrentYear()) {
            timestamp.format(DateTimeFormatter.ofPattern("dd MMM"))
        } else {
            timestamp.format(DateTimeFormatter.ofPattern("dd MMM y"))
        }
    }
}
