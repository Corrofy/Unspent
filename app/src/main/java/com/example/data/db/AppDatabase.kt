package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AppSettingEntity
import com.example.data.model.LedgerEntry
import com.example.data.model.LendingEntry
import com.example.data.model.NoteEntity
import com.example.data.model.PersonEntity

@Database(
    entities = [
        LedgerEntry::class,
        LendingEntry::class,
        NoteEntity::class,
        AppSettingEntity::class,
        PersonEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao
    abstract fun lendingDao(): LendingDao
    abstract fun noteDao(): NoteDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "local_personal_ledger.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

