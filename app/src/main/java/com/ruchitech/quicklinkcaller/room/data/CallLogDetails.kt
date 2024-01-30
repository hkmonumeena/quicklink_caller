package com.ruchitech.quicklinkcaller.room.data

import androidx.compose.ui.graphics.Color
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
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
        var callLogDetails: List<CallLogDetails>
    )

    @Entity(tableName = "Call_logs", indices = [Index(value = ["callerId"], unique = true)])
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
        var cachedName: String?,
        val number: String,
        val type: com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType,
        val date: Long,
        val duration: Long,
        var callNote:String? =null
    ){
        @Ignore
        var colorCode: Color = Color.Black
    }
