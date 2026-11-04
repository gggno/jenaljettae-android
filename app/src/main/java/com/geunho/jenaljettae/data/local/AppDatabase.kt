package com.geunho.jenaljettae.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// MARK: - App Database | Room 데이터베이스 생성 및 관리
@Database(
    entities = [ManagedItemEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // MARK: - DAO | 일정 데이터에 접근하는 인터페이스 제공
    abstract fun managedItemDao(): ManagedItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // MARK: - Migration 1 to 2 | 기존 데이터를 유지하면서 이미지 파일명 컬럼 추가
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE managed_items ADD COLUMN imageFileName TEXT")
            }
        }

        // MARK: - Instance | 앱 전체에서 하나의 데이터베이스 인스턴스 사용
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jenaljettae.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
