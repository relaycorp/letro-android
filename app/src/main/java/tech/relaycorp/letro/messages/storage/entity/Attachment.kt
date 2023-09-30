package tech.relaycorp.letro.messages.storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

const val TABLE_NAME_ATTACHMENTS = "attachments"

@Entity(
    tableName = TABLE_NAME_ATTACHMENTS,
    foreignKeys = [
        ForeignKey(
            entity = Message::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Attachment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val fileId: UUID,
    val path: String,
    val messageId: Long,
)
