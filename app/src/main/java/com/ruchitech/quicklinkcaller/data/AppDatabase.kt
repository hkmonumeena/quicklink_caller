package com.ruchitech.quicklinkcaller.data

// AppDatabase.kt
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReceiverStatus::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiverStatusDao(): ReceiverStatusDao
}
