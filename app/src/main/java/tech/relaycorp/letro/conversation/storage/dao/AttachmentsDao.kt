package tech.relaycorp.letro.conversation.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.conversation.storage.entity.Attachment
import tech.relaycorp.letro.conversation.storage.entity.TABLE_NAME_ATTACHMENTS
import java.util.UUID

@Dao
interface AttachmentsDao {

    @Query("SELECT * FROM $TABLE_NAME_ATTACHMENTS")
    fun getAll(): Flow<List<Attachment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(attachments: List<Attachment>)

    @Query("SELECT * FROM $TABLE_NAME_ATTACHMENTS where fileId=:id")
    suspend fun getById(id: UUID): Attachment?
}
