package com.geunho.jenaljettae.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// MARK: - App Database | 기존 데이터를 유지하면서 일정 필드 추가
@Database(
    entities = [ManagedItemEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun managedItemDao(): ManagedItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // MARK: - Migration 1 to 2 | 이미지 파일명 추가
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE managed_items ADD COLUMN imageFileName TEXT")
            }
        }

        // MARK: - Migration 2 to 3 | 정기 구독 필드 추가
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE managed_items ADD COLUMN subscriptionCycle TEXT")
                db.execSQL("ALTER TABLE managed_items ADD COLUMN subscriptionAmount INTEGER")
            }
        }

        // MARK: - Migration 3 to 4 | 일정 시작 시간 추가
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE managed_items ADD COLUMN scheduleTimeMinutes INTEGER")
            }
        }

        // MARK: - Instance | 데이터베이스 인스턴스 생성
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jenaljettae.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
