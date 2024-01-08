package com.ruchitech.quicklinkcaller.room.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import com.ruchitech.quicklinkcaller.room.Converters


data class CallLogsWithDetails(
    @Embedded val callLogs: CallLogs,
    @Relation(
        parentColumn = "callerId",
        entityColumn = "callerId"
    )
    val callLogDetails: List<CallLogDetails>
)

@Entity(tableName = "Call_logs")
data class CallLogs(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callerId: String,
    var callNote: String? = ""
) {
    @TypeConverters(Converters::class)
    var callLogDetails: List<CallLogDetails> = mutableListOf()
}

@Entity(tableName = "Call_log_details", indices = [Index("callerId"), Index("date")])
data class CallLogDetails(
    @PrimaryKey val id: Long,
    val callerId: String,
    val cachedName: String?,
    val number: String,
    val type: com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType,
    val date: Long,
    val duration: Long
)
