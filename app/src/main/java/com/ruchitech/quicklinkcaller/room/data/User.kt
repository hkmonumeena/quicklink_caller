package com.ruchitech.quicklinkcaller.room.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_table")
data class User(
    @PrimaryKey(autoGenerate = false) val userId: Long,
    val name: String,
    val password: String?,
    // Other fields in version 1
)
