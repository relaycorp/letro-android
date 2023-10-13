package tech.relaycorp.letro.account.model

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import tech.relaycorp.letro.account.model.AccountStatus.Companion.CREATED
import tech.relaycorp.letro.account.model.AccountStatus.Companion.CREATION_WAITING
import tech.relaycorp.letro.account.model.AccountStatus.Companion.ERROR

const val TABLE_NAME_ACCOUNT = "account"

@Entity(
    tableName = TABLE_NAME_ACCOUNT,
    indices = [Index("accountId", unique = true)],
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val accountId: String,
    val requestedUserName: String,
    val normalisedLocale: String?,
    val domain: String,
    val isCurrent: Boolean,
    // TODO: Encrypt key when integrating VeraId (https://relaycorp.atlassian.net/browse/LTR-55)
    val veraidPrivateKey: ByteArray,
    val veraidMemberBundle: ByteArray? = null,
    @AccountStatus val status: Int = CREATION_WAITING,
    val token: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (id != other.id) return false
        if (accountId != other.accountId) return false
        if (requestedUserName != other.requestedUserName) return false
        if (normalisedLocale != other.normalisedLocale) return false
        if (isCurrent != other.isCurrent) return false
        if (!veraidPrivateKey.contentEquals(other.veraidPrivateKey)) return false
        if (veraidMemberBundle != null) {
            if (other.veraidMemberBundle == null) return false
            if (!veraidMemberBundle.contentEquals(other.veraidMemberBundle)) return false
        } else if (other.veraidMemberBundle != null) return false
        if (status != other.status) return false
        if (domain != other.domain) return false
        if (token != other.token) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + accountId.hashCode()
        result = 31 * result + requestedUserName.hashCode()
        result = 31 * result + normalisedLocale.hashCode()
        result = 31 * result + isCurrent.hashCode()
        result = 31 * result + veraidPrivateKey.contentHashCode()
        result = 31 * result + (veraidMemberBundle?.contentHashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + domain.hashCode()
        result = 31 * result + (token?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Account(accountId = $accountId, requestedUserName = $requestedUserName, normalisedLocale = $normalisedLocale, isCurrent = $isCurrent, isCreated = $status)"
    }
}

@IntDef(ERROR, CREATION_WAITING, CREATED)
annotation class AccountStatus {
    companion object {
        const val ERROR = -1
        const val CREATION_WAITING = 0
        const val CREATED = 1
    }
}
