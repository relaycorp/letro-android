package tech.relaycorp.letro.conversation.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.letro.conversation.storage.entity.TABLE_NAME_MESSAGES

@Dao
interface MessagesDao {
    @Insert
    suspend fun insert(message: Message): Long

    @Query("SELECT * FROM $TABLE_NAME_MESSAGES")
    fun getAll(): Flow<List<Message>>
}
