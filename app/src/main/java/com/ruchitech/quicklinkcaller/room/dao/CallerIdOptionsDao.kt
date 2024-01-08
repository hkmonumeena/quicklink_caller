package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruchitech.quicklinkcaller.room.data.CallerIdOptionsEntity

@Dao
interface CallerIdOptionsDao {

    @Query("SELECT * FROM caller_id_options_table WHERE id = 1 LIMIT 1")
    suspend fun getCallerIdOptions(): CallerIdOptionsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCallerIdOptions(entity: CallerIdOptionsEntity)
}
