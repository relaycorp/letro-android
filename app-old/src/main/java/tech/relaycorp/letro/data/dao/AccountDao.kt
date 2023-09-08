package tech.relaycorp.letro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.data.entity.ACCOUNT_TABLE_NAME
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

    @Query("SELECT * FROM $ACCOUNT_TABLE_NAME WHERE veraId=:veraId")
    suspend fun getByVeraId(veraId: String): AccountDataModel?

    @Query("SELECT * FROM $ACCOUNT_TABLE_NAME WHERE id=:id")
    suspend fun getById(id: Long): AccountDataModel?

    @Query("SELECT * FROM $ACCOUNT_TABLE_NAME")
    fun getAll(): Flow<List<AccountDataModel>>

    @Query("DELETE FROM $ACCOUNT_TABLE_NAME")
    suspend fun deleteAll()

    // Use only this method to set current account, not the update above
    @Query("UPDATE $ACCOUNT_TABLE_NAME SET isCurrent = CASE WHEN veraId = :veraId THEN 1 ELSE 0 END")
    suspend fun setCurrentAccount(veraId: String)

    // TODO IDEA for contact, conversation and message adding/deleting
//    @Query("SELECT * FROM $ACCOUNT_TABLE_NAME WHERE id = :accountId")
//    fun getAccountWithContacts(accountId: Long): Flow<AccountWithContacts>
}
