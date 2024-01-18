package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruchitech.quicklinkcaller.room.data.Reminders

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE id ==:id LIMIT 1")
    suspend fun getCallerIdOptions(id: Long): Reminders?

    @Query("SELECT * FROM reminders WHERE callerId ==:number LIMIT 1")
    suspend fun getReminderByCallerId(number: String): Reminders?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCallerIdOptions(reminders: Reminders)
}
