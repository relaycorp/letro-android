package tech.relaycorp.letro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.data.entity.CONVERSATION_TABLE_NAME
import tech.relaycorp.letro.data.entity.ConversationDataModel

@Dao
interface ConversationDao {

    @Query("SELECT * FROM $CONVERSATION_TABLE_NAME")
    fun getAllConversations(): Flow<List<ConversationDataModel>>

    @Query("SELECT * FROM $CONVERSATION_TABLE_NAME WHERE id = :conversationId")
    fun getConversationById(conversationId: Long): Flow<ConversationDataModel>

    @Query("SELECT * FROM $CONVERSATION_TABLE_NAME WHERE contactId = :contactId")
    fun getConversationsForContact(contactId: Long): Flow<List<ConversationDataModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationDataModel)

    @Update
    suspend fun updateConversation(conversation: ConversationDataModel)

    @Delete
    suspend fun deleteConversation(conversation: ConversationDataModel)
}
