package com.ruchitech.quicklinkcaller.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receiver_status")
data class ReceiverStatus(
    @PrimaryKey(autoGenerate = false) val action: Int,
    val isRegistered: Boolean
)