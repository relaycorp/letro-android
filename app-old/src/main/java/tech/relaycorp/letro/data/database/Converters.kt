package tech.relaycorp.letro.data.database

import androidx.room.TypeConverter
import tech.relaycorp.letro.data.entity.PairingStatus

class Converters {
    @TypeConverter
    fun toPairingStatus(value: String): PairingStatus {
        return when (value) {
            "Unpaired" -> PairingStatus.Unpaired
            "RequestSent" -> PairingStatus.RequestSent
            "Match" -> PairingStatus.Match
            "AuthorizationSent" -> PairingStatus.AuthorizationSent
            "Complete" -> PairingStatus.Complete
            else -> throw IllegalArgumentException("Unknown pairing status")
        }
    }

    @TypeConverter
    fun fromPairingStatus(status: PairingStatus): String {
        return when (status) {
            is PairingStatus.Unpaired -> "Unpaired"
            is PairingStatus.RequestSent -> "RequestSent"
            is PairingStatus.Match -> "Match"
            is PairingStatus.AuthorizationSent -> "AuthorizationSent"
            is PairingStatus.Complete -> "Complete"
        }
    }
}
