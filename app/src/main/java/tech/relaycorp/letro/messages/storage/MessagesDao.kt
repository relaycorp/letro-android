package tech.relaycorp.letro.messages.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.messages.storage.entity.Message
import tech.relaycorp.letro.messages.storage.entity.TABLE_NAME_MESSAGES

@Dao
interface MessagesDao {
    @Insert
    suspend fun insert(message: Message): Long

    @Query("SELECT * FROM $TABLE_NAME_MESSAGES")
    fun getAll(): Flow<List<Message>>
}
