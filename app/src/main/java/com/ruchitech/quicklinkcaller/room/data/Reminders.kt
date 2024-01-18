package com.ruchitech.quicklinkcaller.room.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminders(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val time: String,
    val date: String,
    val callerId: String,
    var timeInMillis: Long,
    var status:Boolean // active:true, inactive:false
)
