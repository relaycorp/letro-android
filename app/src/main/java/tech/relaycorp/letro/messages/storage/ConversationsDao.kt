package tech.relaycorp.letro.messages.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.messages.storage.entity.TABLE_NAME_CONVERSATIONS

@Dao
interface ConversationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createNewConversation(conversation: Conversation)

    @Query("SELECT * FROM $TABLE_NAME_CONVERSATIONS")
    fun getAll(): Flow<List<Conversation>>
}
