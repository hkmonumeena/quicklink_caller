package com.ruchitech.quicklinkcaller.room.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timestamp")
data class Timestamp(
    @PrimaryKey val id: Long = 1, // Since there is only one row, set a fixed ID
    var lastCallLogsSync: Long?, // this will ne use to check last synctime of call logs using this instead sharedPref
    @ColumnInfo(name = "last_heartbeat") val lastHeartbeat: Long =0L,
    @ColumnInfo(name = "push_heartbeat") val pushHeartbeat: Long =0L
)
