package tech.relaycorp.letro.contacts.storage

import androidx.room.Dao
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

    @Update
    suspend fun update(contact: Contact)

    @Query("SELECT * FROM $TABLE_NAME_CONTACTS")
    fun getAll(): Flow<List<Contact>>

    @Query("SELECT * FROM $TABLE_NAME_CONTACTS WHERE ownerVeraId = :accountVeraId")
    fun getContactsForAccount(accountVeraId: String): Flow<List<Contact>>
}