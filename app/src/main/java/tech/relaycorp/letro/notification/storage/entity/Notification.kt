package tech.relaycorp.letro.notification.storage.entity

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.notification.storage.entity.NotificationType.Companion.PAIRING_COMPLETED
import java.time.LocalDateTime

const val TABLE_NAME_NOTIFICATIONS = "notifications"

@Entity(
    tableName = TABLE_NAME_NOTIFICATIONS,
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["veraId"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ownerId: String,
    @NotificationType val type: Int,
    val contactVeraId: String,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false,
    val infoSpecificForNotificationType: String? = null,
)

@IntDef(PAIRING_COMPLETED)
annotation class NotificationType {

    companion object {
        const val PAIRING_COMPLETED = 0
    }
}
