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

    @Query("SELECT * FROM $TABLE_NAME_ACCOUNT")
    suspend fun getAllSync(): List<Account>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Account)

    @Update
    suspend fun update(entity: Account): Int

    @Update
    suspend fun update(accounts: List<Account>)

    @Query("SELECT * FROM $TABLE_NAME_ACCOUNT WHERE id=:id")
    suspend fun getById(id: Long): Account?

    @Query("SELECT * FROM $TABLE_NAME_ACCOUNT WHERE accountId=:id")
    suspend fun getByVeraidId(id: String): Account?

    @Query("SELECT * FROM $TABLE_NAME_ACCOUNT WHERE requestedUserName=:requestedUserName AND normalisedLocale=:locale")
    suspend fun getByRequestParams(requestedUserName: String, locale: String): Account?

    @Query("SELECT * FROM $TABLE_NAME_ACCOUNT WHERE domain=:domain")
    suspend fun getByDomain(domain: String): List<Account>

    @Query("SELECT * FROM $TABLE_NAME_ACCOUNT WHERE awalaEndpointId=:awalaEndpoint")
    suspend fun getByAwalaEndpoint(awalaEndpoint: String): List<Account>

    @Query("SELECT * FROM $TABLE_NAME_ACCOUNT WHERE firstPartyEndpointNodeId=:firstPartyEndpointNodeId")
    suspend fun getByFirstPartyEndpointNodeId(firstPartyEndpointNodeId: String): Account?

    @Delete
    suspend fun deleteAccount(account: Account)
}
