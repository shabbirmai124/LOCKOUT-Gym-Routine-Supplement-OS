package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ExerciseEntity::class,
        SupplementEntity::class,
        SupplementLogEntity::class,
        DailyNutritionEntity::class,
        VaultItemEntity::class,
        SkillMilestoneEntity::class,
        AppPrefsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LockoutTypeConverters::class)
abstract class LockoutDatabase : RoomDatabase() {
    abstract fun lockoutDao(): LockoutDao

    companion object {
        @Volatile
        private var INSTANCE: LockoutDatabase? = null

        fun getInstance(context: Context): LockoutDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LockoutDatabase::class.java,
                    "lockout_os.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
