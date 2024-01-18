package com.ruchitech.quicklinkcaller.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ruchitech.quicklinkcaller.room.dao.CallLogDao
import com.ruchitech.quicklinkcaller.room.dao.CallerIdOptionsDao
import com.ruchitech.quicklinkcaller.room.dao.ContactDao
import com.ruchitech.quicklinkcaller.room.dao.DataDao
import com.ruchitech.quicklinkcaller.room.dao.ReminderDao
import com.ruchitech.quicklinkcaller.room.dao.TimestampDao
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogs
import com.ruchitech.quicklinkcaller.room.data.CallerIdOptionsEntity
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.room.data.Timestamp
import com.ruchitech.quicklinkcaller.room.data.User


@Database(
    entities = [
        User::class,
        CallLogs::class,
        CallLogDetails::class,
        Contact::class,
        Timestamp::class,
        CallerIdOptionsEntity::class,
        Reminders::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DatabaseDao : RoomDatabase() {
    abstract fun dataDao(): DataDao
    abstract fun callLogs(): CallLogDao
    abstract fun contact(): ContactDao
    abstract fun callerIDOptions(): CallerIdOptionsDao
    abstract fun timestampDao(): TimestampDao
    abstract fun reminders(): ReminderDao
}