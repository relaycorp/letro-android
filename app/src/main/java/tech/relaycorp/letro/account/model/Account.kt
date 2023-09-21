package tech.relaycorp.letro.account.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

const val TABLE_NAME_ACCOUNT = "account"

@Entity(
    tableName = TABLE_NAME_ACCOUNT,
    indices = [Index("veraId", unique = true)],
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val veraId: String,
    val isCurrent: Boolean,
    val isCreated: Boolean = false,
)
