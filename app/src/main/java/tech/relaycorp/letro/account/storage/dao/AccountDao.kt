package tech.relaycorp.letro.account.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.model.TABLE_NAME_ACCOUNT

@Dao
interface AccountDao {
    @Query("SELECT * FROM $TABLE_NAME_ACCOUNT")
    fun getAll(): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Account)

    @Update
    suspend fun update(entity: Account): Int

    @Query("SELECT * FROM $TABLE_NAME_ACCOUNT WHERE id=:id")
    suspend fun getById(id: Long): Account?

    @Query("SELECT * FROM $TABLE_NAME_ACCOUNT WHERE requestedUserName=:requestedUserName AND normalisedLocale=:locale")
    suspend fun getByRequestParams(requestedUserName: String, locale: String): Account?

    @Delete
    suspend fun deleteAccount(account: Account)
}
