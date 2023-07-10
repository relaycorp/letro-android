package tech.relaycorp.letro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.data.entity.USER_TABLE_NAME
import tech.relaycorp.letro.data.entity.AccountDataModel

@Dao
interface AccountDao {

    @Insert
    suspend fun insert(entity: AccountDataModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entities: AccountDataModel): LongArray

    @Update
    suspend fun update(entity: AccountDataModel): Int

    @Delete
    suspend fun delete(entity: AccountDataModel): Int

    @Query("SELECT * FROM $USER_TABLE_NAME WHERE nodeId=:nodeId")
    suspend fun getByNodeId(nodeId: String): AccountDataModel

    @Transaction
    @Query("SELECT * FROM $USER_TABLE_NAME")
    fun getAll(): Flow<List<AccountDataModel>>

    @Query("DELETE FROM $USER_TABLE_NAME")
    suspend fun deleteAll()
}