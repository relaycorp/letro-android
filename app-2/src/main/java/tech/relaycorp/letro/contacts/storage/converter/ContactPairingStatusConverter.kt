package tech.relaycorp.letro.contacts.storage.converter

import androidx.room.TypeConverter
import tech.relaycorp.letro.contacts.model.ContactPairingStatus

class ContactPairingStatusConverter {
    @TypeConverter
    fun toPairingStatus(value: String): ContactPairingStatus {
        return when (value) {
            "Unpaired" -> ContactPairingStatus.Unpaired
            "RequestSent" -> ContactPairingStatus.RequestSent
            "Match" -> ContactPairingStatus.Match
            "AuthorizationSent" -> ContactPairingStatus.AuthorizationSent
            "Complete" -> ContactPairingStatus.Complete
            else -> throw IllegalArgumentException("Unknown pairing status")
        }
    }

    @TypeConverter
    fun fromPairingStatus(status: ContactPairingStatus): String {
        return when (status) {
            is ContactPairingStatus.Unpaired -> "Unpaired"
            is ContactPairingStatus.RequestSent -> "RequestSent"
            is ContactPairingStatus.Match -> "Match"
            is ContactPairingStatus.AuthorizationSent -> "AuthorizationSent"
            is ContactPairingStatus.Complete -> "Complete"
        }
    }
}