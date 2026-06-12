package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Folder::class,
        VaultFile::class,
        CalcSetting::class,
        CalcHistory::class,
        Feedback::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun vaultFileDao(): VaultFileDao
    abstract fun calcSettingDao(): CalcSettingDao
    abstract fun calcHistoryDao(): CalcHistoryDao
    abstract fun feedbackDao(): FeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jiuyi_calculator_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
