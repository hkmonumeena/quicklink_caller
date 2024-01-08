package com.ruchitech.quicklinkcaller.data

// ReceiverStatusDao.kt
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruchitech.quicklinkcaller.data.ReceiverStatus

@Dao
interface ReceiverStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiverStatus(receiverStatus: ReceiverStatus)

    @Query("SELECT * FROM receiver_status WHERE `action` = :action")
    suspend fun getReceiverStatus(action: Int): ReceiverStatus?

    @Query("SELECT * FROM receiver_status")
    suspend fun getAllReceiverStatus(): List<ReceiverStatus>
}
