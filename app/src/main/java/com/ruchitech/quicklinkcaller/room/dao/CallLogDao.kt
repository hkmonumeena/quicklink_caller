package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.*
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogs
import com.ruchitech.quicklinkcaller.room.data.CallLogsWithDetails
import com.ruchitech.quicklinkcaller.room.data.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLogs(callLogs: List<CallLogs>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCallLog(callLogs: CallLogs): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCallLogDetail(callLogDetails: CallLogDetails): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLogDetails(callLogDetails: List<CallLogDetails>)

    @Query("SELECT COUNT(*) FROM Call_logs WHERE callerId = :callerId")
    suspend fun doesCallerIdExist(callerId: String): Int

    @Query("SELECT COUNT(*) FROM Call_log_details WHERE id = :callLogId")
    suspend fun doesCallLogExist(callLogId: Long): Int

    @Transaction
    @Query("SELECT * FROM Call_logs ORDER BY (SELECT MAX(date) FROM Call_log_details WHERE callerId = Call_logs.callerId) DESC")
    fun getAllCallLogsWithDetails(): Flow<List<CallLogsWithDetails>>

    @Query("SELECT * FROM Call_logs ORDER BY (SELECT MAX(date) FROM Call_log_details WHERE callerId = Call_logs.callerId) DESC LIMIT :pageSize OFFSET :offset")
    fun getPaginatedCallLogs(pageSize: Int, offset: Int): Flow<List<CallLogsWithDetails>>

    // Query to get a CallLogs object based on callerId
    @Query("SELECT * FROM Call_logs WHERE callerId = :callerId")
    suspend fun getCallLogsByCallerId(callerId: String): CallLogs?

    // Insert or update a CallLogs entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCallLogs(callLogs: CallLogs)

    // Insert or update multiple CallLogDetails entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCallLogDetails(callLogDetails: List<CallLogDetails>)
}