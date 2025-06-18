package com.example.lungradarapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class AnalysisResult(
    val id: Int,
    val resultType: String,
    val imagePath: String,
    val confidence: Double,
    val riskAssessment: String,
    val timestamp: Long
)

class LungCancerDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "lung_cancer_results.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "results"
        private const val COLUMN_ID = "id"
        private const val COLUMN_RESULT_TYPE = "resultType"
        private const val COLUMN_IMAGE_PATH = "imagePath"
        private const val COLUMN_CONFIDENCE = "confidence"
        private const val COLUMN_RISK_ASSESSMENT = "riskAssessment"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RESULT_TYPE TEXT,
                $COLUMN_IMAGE_PATH TEXT,
                $COLUMN_CONFIDENCE REAL,
                $COLUMN_RISK_ASSESSMENT TEXT,
                $COLUMN_TIMESTAMP INTEGER
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertResult(resultType: String, imagePath: String, confidence: Double, riskAssessment: String, timestamp: Long): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RESULT_TYPE, resultType)
            put(COLUMN_IMAGE_PATH, imagePath)
            put(COLUMN_CONFIDENCE, confidence)
            put(COLUMN_RISK_ASSESSMENT, riskAssessment)
            put(COLUMN_TIMESTAMP, timestamp)
        }

        return try {
            val id = db.insert(TABLE_NAME, null, values)
            id
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        } finally {
            db.close()
        }
    }

    fun getAllResults(): List<AnalysisResult> {
        val results = mutableListOf<AnalysisResult>()
        val db = this.readableDatabase

        db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_TIMESTAMP DESC", null).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID)
            val resultTypeIndex = cursor.getColumnIndexOrThrow(COLUMN_RESULT_TYPE)
            val imagePathIndex = cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)
            val confidenceIndex = cursor.getColumnIndexOrThrow(COLUMN_CONFIDENCE)
            val riskAssessmentIndex = cursor.getColumnIndexOrThrow(COLUMN_RISK_ASSESSMENT)
            val timestampIndex = cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)

            while (cursor.moveToNext()) {
                val result = AnalysisResult(
                    id = cursor.getInt(idIndex),
                    resultType = cursor.getString(resultTypeIndex),
                    imagePath = cursor.getString(imagePathIndex),
                    confidence = cursor.getDouble(confidenceIndex),
                    riskAssessment = cursor.getString(riskAssessmentIndex),
                    timestamp = cursor.getLong(timestampIndex)
                )
                results.add(result)
            }
        }

        db.close()
        return results
    }

    fun deleteResult(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }
}
