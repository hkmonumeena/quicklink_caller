package com.ruchitech.quicklinkcaller.room


import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideYourDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        DatabaseDao::class.java,
        "test_task_db"
    )//.addMigrations(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4)
        .build()

    @Singleton
    @Provides
    fun provideYourDao(db: DatabaseDao) = db.dataDao()
}
/*
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE data_table ADD COLUMN password TEXT")
        }
    }
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create a temporary table with the new schema
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS Call_logs_temp (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "callerId TEXT NOT NULL," +
                        "callNote TEXT," +  // Make callNote nullable
                        "UNIQUE(callerId) ON CONFLICT REPLACE" +
                        ")"
            )

            // Copy data from the old table to the new table
            database.execSQL(
                "INSERT INTO Call_logs_temp (id, callerId, callNote) SELECT id, callerId, callNote FROM Call_logs"
            )

            // Remove the old table
            database.execSQL("DROP TABLE Call_logs")

            // Rename the new table to the original table name
            database.execSQL("ALTER TABLE Call_logs_temp RENAME TO Call_logs")
        }
    }
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create a temporary table with the new schema
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS Call_logs_temp (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "callerId TEXT NOT NULL," +
                        "callNote TEXT," +  // Make callNote nullable
                        "UNIQUE(callerId) ON CONFLICT REPLACE" +
                        ")"
            )

            // Copy data from the old table to the new table (excluding callLogDetails)
            database.execSQL(
                "INSERT INTO Call_logs_temp (id, callerId, callNote) SELECT id, callerId, '' FROM Call_logs"
            )

            // Remove the old table
            database.execSQL("DROP TABLE Call_logs")

            // Rename the new table to the original table name
            database.execSQL("ALTER TABLE Call_logs_temp RENAME TO Call_logs")
        }
    }
}*/
