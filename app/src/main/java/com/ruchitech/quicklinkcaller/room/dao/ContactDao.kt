package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruchitech.quicklinkcaller.room.data.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    // Insert multiple contacts at once
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg contacts: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getContactByPhoneNumber(phoneNumber: String): Contact?

    @Query("SELECT * FROM contacts")
    fun getAllContacts(): Flow<List<Contact>>

    // Updated query with pagination and sorting
    @Query("SELECT * FROM contacts ORDER BY name ASC LIMIT :pageSize OFFSET :offset")
    fun getContactsPagedSortedByName(pageSize: Int, offset: Int): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE LOWER(name) LIKE LOWER('%' || :searchQuery || '%') OR phoneNumber LIKE '%' || :searchQuery || '%'")
    suspend fun searchContactsByNameOrNumberSortedByName(searchQuery: String): List<Contact>
}
