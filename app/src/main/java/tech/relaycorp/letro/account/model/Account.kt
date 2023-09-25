package tech.relaycorp.letro.account.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

const val TABLE_NAME_ACCOUNT = "account"

@Entity(
    tableName = TABLE_NAME_ACCOUNT,
    indices = [Index("veraidId", unique = true)],
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val veraidId: String,
    val requestedUserName: String,
    val locale: String,
    val isCurrent: Boolean,
    // TODO: Encrypt key when integrating VeraId (https://relaycorp.atlassian.net/browse/LTR-55)
    val veraidPrivateKey: ByteArray,
    val veraidMemberBundle: ByteArray? = null,
    val isCreated: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (id != other.id) return false
        if (veraidId != other.veraidId) return false
        if (requestedUserName != other.requestedUserName) return false
        if (locale != other.locale) return false
        if (isCurrent != other.isCurrent) return false
        if (!veraidPrivateKey.contentEquals(other.veraidPrivateKey)) return false
        if (veraidMemberBundle != null) {
            if (other.veraidMemberBundle == null) return false
            if (!veraidMemberBundle.contentEquals(other.veraidMemberBundle)) return false
        } else if (other.veraidMemberBundle != null) return false
        if (isCreated != other.isCreated) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + veraidId.hashCode()
        result = 31 * result + requestedUserName.hashCode()
        result = 31 * result + locale.hashCode()
        result = 31 * result + isCurrent.hashCode()
        result = 31 * result + veraidPrivateKey.contentHashCode()
        result = 31 * result + (veraidMemberBundle?.contentHashCode() ?: 0)
        result = 31 * result + isCreated.hashCode()
        return result
    }
}