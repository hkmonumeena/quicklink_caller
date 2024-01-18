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
    @Query("UPDATE timestamp SET last_heartbeat = :lastHeartbeat WHERE id = 1")
    suspend fun updateLastHeartbeat(lastHeartbeat: Long)
    @Query("UPDATE timestamp SET push_heartbeat = :pushHeartbeat WHERE id = 1")
    suspend fun updatePushHeartbeat(pushHeartbeat: Long)

    @Query("SELECT last_heartbeat FROM timestamp WHERE id = 1 LIMIT 1")
    suspend fun getLastHeartbeat(): Long?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTimestamp(entity: Timestamp)
}
