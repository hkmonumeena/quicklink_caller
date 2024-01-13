package com.ruchitech.quicklinkcaller.room.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions

@Entity(tableName = "caller_id_options_table")
data class CallerIdOptionsEntity(
    @PrimaryKey val id: Long = 1, // Since there is only one row, set a fixed ID
    @ColumnInfo(name = "caller_id_options") val callerIdOptions: Set<AllCallerIdOptions>,
    @ColumnInfo(name = "last_heartbeat") val lastHeartbeat: Long =0L
)
