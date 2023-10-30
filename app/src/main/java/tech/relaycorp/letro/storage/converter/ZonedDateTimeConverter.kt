package tech.relaycorp.letro.storage.converter

import androidx.room.TypeConverter
import java.time.ZonedDateTime

class ZonedDateTimeConverter {
    @TypeConverter
    fun toDate(dateString: String): ZonedDateTime? {
        return ZonedDateTime.parse(dateString)
    }

    @TypeConverter
    fun toDateString(date: ZonedDateTime): String {
        return date.toString()
    }
}
