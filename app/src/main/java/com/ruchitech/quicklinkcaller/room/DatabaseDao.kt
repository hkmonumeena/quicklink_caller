package com.ruchitech.quicklinkcaller.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ruchitech.quicklinkcaller.room.dao.CallLogDao
import com.ruchitech.quicklinkcaller.room.dao.CallerIdOptionsDao
import com.ruchitech.quicklinkcaller.room.dao.ContactDao
import com.ruchitech.quicklinkcaller.room.dao.DataDao
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogs
import com.ruchitech.quicklinkcaller.room.data.CallerIdOptionsEntity
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.room.data.User


@Database(
    entities = [
        User::class,
        CallLogs::class,
        CallLogDetails::class,
        Contact::class,
    CallerIdOptionsEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DatabaseDao : RoomDatabase() {
    abstract fun dataDao(): DataDao
    abstract fun callLogs(): CallLogDao
    abstract fun contact(): ContactDao
    abstract fun callerIDOptions(): CallerIdOptionsDao
}