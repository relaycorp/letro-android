package tech.relaycorp.letro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.data.entity.MESSAGE_TABLE_NAME
import tech.relaycorp.letro.data.entity.MessageDataModel

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageDataModel)

    @Query("SELECT * FROM $MESSAGE_TABLE_NAME WHERE conversationId = :contactId")
    fun getMessagesForContact(contactId: Long): Flow<List<MessageDataModel>>

    // more methods as needed...
}
