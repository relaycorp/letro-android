package tech.relaycorp.letro.contacts.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.TABLE_NAME_CONTACTS

@Dao
interface ContactsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact): Long

    @Query("SELECT * FROM $TABLE_NAME_CONTACTS WHERE ownerVeraId = :ownerVeraId AND contactVeraId = :contactVeraId")
    suspend fun getContact(
        ownerVeraId: String,
        contactVeraId: String,
    ): Contact?

    @Update
    suspend fun update(contact: Contact)

    @Query("SELECT * FROM $TABLE_NAME_CONTACTS")
    fun getAll(): Flow<List<Contact>>

    @Query("SELECT * FROM $TABLE_NAME_CONTACTS WHERE ownerVeraId = :accountVeraId")
    fun getContactsForAccount(accountVeraId: String): Flow<List<Contact>>

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("SELECT * FROM $TABLE_NAME_CONTACTS WHERE contactEndpointId = :contactEndpointId")
    suspend fun getContactsByContactEndpointId(contactEndpointId: String): List<Contact>
}
