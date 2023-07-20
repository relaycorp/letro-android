package tech.relaycorp.letro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

const val CONTACT_TABLE_NAME = "contact"

@Entity(tableName = CONTACT_TABLE_NAME)
data class ContactDataModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val address: String,
    val alias: String,
    val contactEndpointId: String? = null,
    val contactEndpointPublicKey: ByteArray? = null,
    val status: PairingStatus = PairingStatus.Unpaired,
    val conversations: List<ConversationDataModel> = emptyList(),
)

sealed interface PairingStatus {
    object Unpaired : PairingStatus
    object RequestSent : PairingStatus
    object Match : PairingStatus
    object AuthorizationSent : PairingStatus
    object Complete : PairingStatus
}
