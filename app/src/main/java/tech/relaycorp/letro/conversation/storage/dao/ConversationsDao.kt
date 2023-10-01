package tech.relaycorp.letro.conversation.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.TABLE_NAME_CONVERSATIONS
import java.util.UUID

@Dao
interface ConversationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createNewConversation(conversation: Conversation)

    @Query("SELECT * FROM $TABLE_NAME_CONVERSATIONS")
    fun getAll(): Flow<List<Conversation>>

    @Query("SELECT * FROM $TABLE_NAME_CONVERSATIONS WHERE conversationId = :conversationId")
    suspend fun getConversationById(conversationId: UUID): Conversation?

    @Update
    suspend fun update(conversation: Conversation)

    @Delete
    suspend fun delete(conversation: Conversation)
}
