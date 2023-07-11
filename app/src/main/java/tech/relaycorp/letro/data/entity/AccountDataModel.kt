package tech.relaycorp.letro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

const val ACCOUNT_TABLE_NAME = "account"

@Entity(tableName = ACCOUNT_TABLE_NAME)
data class AccountDataModel(
    @PrimaryKey
    val id: Long,
    val address: String,
//    val contacts: List<String> = emptyList(), // TODO Add Contact entity here
)
