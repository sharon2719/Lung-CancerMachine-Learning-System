package com.example.lungradarapp

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Creating the analysis_results table when upgrading from version 1 to 2
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `analysis_results` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `result` TEXT NOT NULL,
                    `confidence` REAL NOT NULL,
                    `timestamp` INTEGER NOT NULL
                )
            """
        )
    }
}
