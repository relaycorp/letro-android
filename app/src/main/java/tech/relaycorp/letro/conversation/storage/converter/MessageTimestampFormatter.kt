package tech.relaycorp.letro.conversation.storage.converter

import android.content.Context
import android.text.format.DateFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import tech.relaycorp.letro.utils.time.isCurrentYear
import tech.relaycorp.letro.utils.time.isToday
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface MessageTimestampFormatter {
    fun formatBrief(timestamp: ZonedDateTime): String
    fun formatDetailed(timestamp: ZonedDateTime): String
}

class MessageTimestampFormatterImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : MessageTimestampFormatter {

    override fun formatBrief(timestamp: ZonedDateTime): String {
        return if (timestamp.isToday()) {
            timestamp.format(DateTimeFormatter.ofPattern("${getHoursPattern()}:mm${getAmPmPostfixPattern()}"))
        } else if (timestamp.isCurrentYear()) {
            timestamp.format(DateTimeFormatter.ofPattern("dd MMM"))
        } else {
            timestamp.format(DateTimeFormatter.ofPattern("dd MMM y"))
        }
    }

    override fun formatDetailed(timestamp: ZonedDateTime): String {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM y, ${getHoursPattern()}:mm${getAmPmPostfixPattern()}"))
    }

    private fun getHoursPattern() = if (DateFormat.is24HourFormat(context)) "HH" else "hh"

    private fun getAmPmPostfixPattern() = if (DateFormat.is24HourFormat(context)) "" else " a"
}
