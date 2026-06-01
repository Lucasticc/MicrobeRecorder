package com.microbe.recorder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RecordEntity::class, SampleTypeEntity::class, TreatmentGroupEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao
    abstract fun sampleTypeDao(): SampleTypeDao
    abstract fun treatmentGroupDao(): TreatmentGroupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 版本 1 → 2：新增 sample_types 表
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS sample_types (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        // 版本 2 → 3：重构 microbe_records + 新增 treatment_groups
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建处理组表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS treatment_groups (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        plantingDate TEXT NOT NULL DEFAULT '',
                        description TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // 重建记录表（新结构）
                database.execSQL("DROP TABLE IF EXISTS microbe_records")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS microbe_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        plantingDate TEXT NOT NULL DEFAULT '',
                        treatmentGroup TEXT NOT NULL DEFAULT '',
                        observationResult TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        photoPaths TEXT NOT NULL DEFAULT '',
                        audioPath TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "microbe_recorder_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
