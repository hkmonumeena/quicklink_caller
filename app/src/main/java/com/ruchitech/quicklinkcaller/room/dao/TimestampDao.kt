package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruchitech.quicklinkcaller.room.data.Timestamp

@Dao
interface TimestampDao {

    @Query("SELECT * FROM timestamp WHERE id = 1 LIMIT 1")
    suspend fun getTimestamps(): Timestamp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTimestamp(entity: Timestamp)
}
