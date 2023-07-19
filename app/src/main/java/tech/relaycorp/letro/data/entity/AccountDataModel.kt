package tech.relaycorp.letro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

const val ACCOUNT_TABLE_NAME = "account"

@Entity(tableName = ACCOUNT_TABLE_NAME)
data class AccountDataModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val address: String,
    val isCurrent: Boolean = false,
    val isCreationConfirmed: Boolean = false,
    val contacts: List<ContactDataModel> = emptyList(),
)
