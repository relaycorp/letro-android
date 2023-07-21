package tech.relaycorp.letro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

const val CONTACT_TABLE_NAME = "contact"

@Entity(
    tableName = CONTACT_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = AccountDataModel::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("accountId")],
)
data class ContactDataModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val accountId: Long,
    val address: String,
    val alias: String,
    val contactEndpointId: String? = null,
    val status: PairingStatus = PairingStatus.Unpaired,
)

sealed interface PairingStatus {
    object Unpaired : PairingStatus
    object RequestSent : PairingStatus
    object Match : PairingStatus
    object AuthorizationSent : PairingStatus
    object Complete : PairingStatus
}
