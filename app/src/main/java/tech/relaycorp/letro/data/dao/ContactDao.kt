package tech.relaycorp.letro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.data.entity.CONTACT_TABLE_NAME
import tech.relaycorp.letro.data.entity.ContactDataModel

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactDataModel)

    @Update
    suspend fun update(contact: ContactDataModel)

    @Query("SELECT * FROM $CONTACT_TABLE_NAME")
    fun getAll(): Flow<List<ContactDataModel>>

    @Query("SELECT * FROM $CONTACT_TABLE_NAME WHERE accountId = :accountId")
    fun getContactsForAccount(accountId: Long): Flow<List<ContactDataModel>>

    // Get contact by address and accountId
    @Query("SELECT * FROM $CONTACT_TABLE_NAME WHERE address = :address AND accountId = :accountId")
    suspend fun getContactByAddress(address: String, accountId: Long): ContactDataModel?

    @Query("SELECT * FROM $CONTACT_TABLE_NAME WHERE contactEndpointId = :contactEndpointId")
    suspend fun getContactsByContactEndpointId(contactEndpointId: String): List<ContactDataModel>
}
