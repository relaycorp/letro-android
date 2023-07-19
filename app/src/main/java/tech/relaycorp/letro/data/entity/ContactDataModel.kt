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
    val status: ContactStatus = ContactStatus.Unpaired,
    val conversations: List<ConversationDataModel> = emptyList(),
)

sealed interface ContactStatus {
    object Unpaired : ContactStatus
    object PairingRequestSent : ContactStatus
    object PairingMatch : ContactStatus
    object AuthorizationSent : ContactStatus
    object AuthorizationReceived : ContactStatus
    object PairedAndAuthorized : ContactStatus
}