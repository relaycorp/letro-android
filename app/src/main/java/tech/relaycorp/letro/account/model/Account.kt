package tech.relaycorp.letro.account.model

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import tech.relaycorp.letro.account.model.AccountStatus.Companion.CREATED
import tech.relaycorp.letro.account.model.AccountStatus.Companion.CREATION_WAITING
import tech.relaycorp.letro.account.model.AccountStatus.Companion.ERROR_CREATION
import tech.relaycorp.letro.account.model.AccountStatus.Companion.ERROR_LINKING
import tech.relaycorp.letro.account.model.AccountStatus.Companion.LINKING_WAITING
import tech.relaycorp.letro.account.model.AccountType.Companion.CREATED_FROM_SCRATCH
import tech.relaycorp.letro.account.model.AccountType.Companion.LINKED_EXISTING

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
    @AccountType val accountType: Int,
    // TODO: Encrypt key when integrating VeraId (https://relaycorp.atlassian.net/browse/LTR-55)
    val veraidPrivateKey: ByteArray,
    val veraidMemberBundle: ByteArray? = null,
    val awalaEndpointId: String? = null,
    val veraidAuthEndpointId: String? = null,
    @AccountStatus val status: Int = CREATION_WAITING,
    val token: String? = null,
    val avatarPath: String? = null,
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
        if (awalaEndpointId != other.awalaEndpointId) return false
        if (accountType != other.accountType) return false
        if (veraidAuthEndpointId != other.veraidAuthEndpointId) return false
        if (!veraidPrivateKey.contentEquals(other.veraidPrivateKey)) return false
        if (veraidMemberBundle != null) {
            if (other.veraidMemberBundle == null) return false
            if (!veraidMemberBundle.contentEquals(other.veraidMemberBundle)) return false
        } else if (other.veraidMemberBundle != null) return false
        if (status != other.status) return false
        if (domain != other.domain) return false
        if (token != other.token) return false
        if (avatarPath != other.avatarPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + accountId.hashCode()
        result = 31 * result + requestedUserName.hashCode()
        result = 31 * result + normalisedLocale.hashCode()
        result = 31 * result + isCurrent.hashCode()
        result = 31 * result + awalaEndpointId.hashCode()
        result = 31 * result + veraidAuthEndpointId.hashCode()
        result = 31 * result + accountType.hashCode()
        result = 31 * result + veraidPrivateKey.contentHashCode()
        result = 31 * result + (veraidMemberBundle?.contentHashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + domain.hashCode()
        result = 31 * result + (token?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Account(accountId = $accountId, requestedUserName = $requestedUserName, normalisedLocale = $normalisedLocale, isCurrent = $isCurrent, isCreated = $status; accountType = $accountType)"
    }
}

@IntDef(ERROR_LINKING, ERROR_CREATION, CREATION_WAITING, LINKING_WAITING, CREATED)
annotation class AccountStatus {
    companion object {
        const val ERROR_CREATION = -2
        const val ERROR_LINKING = -1
        const val CREATION_WAITING = 0
        const val LINKING_WAITING = 1
        const val CREATED = 2
    }
}

@IntDef(CREATED_FROM_SCRATCH, LINKED_EXISTING)
annotation class AccountType {
    companion object {
        const val CREATED_FROM_SCRATCH = 1
        const val LINKED_EXISTING = 2
    }
}
