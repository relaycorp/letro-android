package tech.relaycorp.letro.contacts.model

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.contacts.model.ContactPairingStatus.Companion.AUTHORIZATION_SENT
import tech.relaycorp.letro.contacts.model.ContactPairingStatus.Companion.COMPLETED
import tech.relaycorp.letro.contacts.model.ContactPairingStatus.Companion.MATCH
import tech.relaycorp.letro.contacts.model.ContactPairingStatus.Companion.REQUEST_SENT
import tech.relaycorp.letro.contacts.model.ContactPairingStatus.Companion.UNPAIRED

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
    indices = [Index("ownerVeraId")],
)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ownerVeraId: String,
    val contactVeraId: String,
    val alias: String? = null,
    val contactEndpointId: String? = null,
    @ContactPairingStatus val status: Int = UNPAIRED,
)

@IntDef(UNPAIRED, REQUEST_SENT, MATCH, AUTHORIZATION_SENT, COMPLETED)
annotation class ContactPairingStatus {
    companion object {
        const val UNPAIRED = 0
        const val REQUEST_SENT = 1
        const val MATCH = 2
        const val AUTHORIZATION_SENT = 3
        const val COMPLETED = 4
    }
}
