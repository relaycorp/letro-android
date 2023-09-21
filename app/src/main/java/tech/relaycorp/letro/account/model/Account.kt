package tech.relaycorp.letro.account.model

import androidx.room.Entity
import androidx.room.PrimaryKey

const val TABLE_NAME_ACCOUNT = "account"

@Entity(
    tableName = TABLE_NAME_ACCOUNT,
)
data class Account(
    @PrimaryKey
    val veraId: String,
    val requestedUserName: String,
    val locale: String,
    val isCurrent: Boolean,
    val isCreated: Boolean = false,
)
