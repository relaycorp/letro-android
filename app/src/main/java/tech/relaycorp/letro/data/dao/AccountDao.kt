package tech.relaycorp.letro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.data.entity.ACCOUNT_TABLE_NAME
import tech.relaycorp.letro.data.entity.AccountDataModel

@Dao
interface AccountDao {

    @Insert
    suspend fun insert(entity: AccountDataModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entities: AccountDataModel): LongArray

    @Delete
    suspend fun delete(entity: AccountDataModel): Int

    @Query("SELECT * FROM $ACCOUNT_TABLE_NAME WHERE address=:address")
    suspend fun getByAddress(address: String): AccountDataModel

    @Query("SELECT * FROM $ACCOUNT_TABLE_NAME WHERE id=:id")
    suspend fun getById(id: Long): AccountDataModel

    @Query("SELECT * FROM $ACCOUNT_TABLE_NAME")
    fun getAll(): Flow<List<AccountDataModel>>

    @Query("DELETE FROM $ACCOUNT_TABLE_NAME")
    suspend fun deleteAll()

    @Query("UPDATE $ACCOUNT_TABLE_NAME SET isCurrent = CASE WHEN address = :address THEN 1 ELSE 0 END")
    suspend fun setCurrentAccount(address: String)
}
