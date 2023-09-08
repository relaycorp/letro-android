package tech.relaycorp.letro.contacts.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import tech.relaycorp.letro.account.model.Account

const val TABLE_NAME_CONTACTS = "contacts"

@Entity(
    tableName = TABLE_NAME_CONTACTS,
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["veraId"],
            childColumns = ["ownerVeraId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val ownerVeraId: String,
    val contactVeraId: String,
    val alias: String? = null,
    val contactEndpointId: String? = null,
    val status: ContactPairingStatus = ContactPairingStatus.Unpaired,
)

sealed interface ContactPairingStatus {
    object Unpaired : ContactPairingStatus
    object RequestSent : ContactPairingStatus
    object Match : ContactPairingStatus
    object AuthorizationSent : ContactPairingStatus
    object Complete : ContactPairingStatus
}