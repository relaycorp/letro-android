package tech.relaycorp.letro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

const val USER_TABLE_NAME = "user"

@Entity(tableName = USER_TABLE_NAME)
data class AccountDataModel(
    @PrimaryKey
    val nodeId: String,
    val username: String,
    val connections: List<String> = emptyList(),
)
