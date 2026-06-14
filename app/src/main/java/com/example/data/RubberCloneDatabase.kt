package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AnalysisEntity::class, UserEntity::class], version = 1, exportSchema = false)
abstract class RubberCloneDatabase : RoomDatabase() {
    abstract fun rubberCloneDao(): RubberCloneDao

    companion object {
        @Volatile
        private var INSTANCE: RubberCloneDatabase? = null

        fun getDatabase(context: Context): RubberCloneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RubberCloneDatabase::class.java,
                    "rubber_clone_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
