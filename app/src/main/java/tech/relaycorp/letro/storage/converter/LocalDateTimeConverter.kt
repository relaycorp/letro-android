package tech.relaycorp.letro.storage.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime

class LocalDateTimeConverter {
    @TypeConverter
    fun toDate(dateString: String): LocalDateTime? {
        return LocalDateTime.parse(dateString)
    }

    @TypeConverter
    fun toDateString(date: LocalDateTime): String {
        return date.toString()
    }
}
