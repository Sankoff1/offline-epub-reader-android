package com.chitalka.storage

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.chitalka.debug.ChitalkaMirrorLog

internal class ChitalkaSqliteOpenHelper(
    context: Context,
) : SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DB_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        if (!db.isReadOnly) {
            db.enableWriteAheadLogging()
            try {
                db.execSQL("PRAGMA synchronous=NORMAL;")
                db.execSQL("PRAGMA temp_store=MEMORY;")
                db.execSQL("PRAGMA foreign_keys=ON;")
            } catch (e: SQLException) {
                ChitalkaMirrorLog.e(TAG, "pragma tuning failed", e)
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_READING_PROGRESS (
              book_id TEXT PRIMARY KEY NOT NULL,
              last_chapter_index INTEGER NOT NULL,
              scroll_offset REAL NOT NULL,
              scroll_range_max REAL NOT NULL DEFAULT 0,
              last_read_timestamp INTEGER NOT NULL
            );
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_LIBRARY_BOOKS (
              book_id TEXT PRIMARY KEY NOT NULL,
              file_uri TEXT NOT NULL,
              title TEXT NOT NULL,
              author TEXT NOT NULL,
              file_size_bytes INTEGER NOT NULL,
              cover_uri TEXT,
              added_at INTEGER NOT NULL,
              total_chapters INTEGER NOT NULL DEFAULT 0,
              is_favorite INTEGER NOT NULL DEFAULT 0,
              deleted_at INTEGER
            );
            """.trimIndent(),
        )
        createIndexes(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Идемпотентные миграции выполняются в onOpen.
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        migrateLibraryColumnsIfNeeded(db)
        migrateReadingProgressColumnsIfNeeded(db)
        createIndexes(db)
    }

    private fun migrateLibraryColumnsIfNeeded(db: SQLiteDatabase) {
        val columns = listLibraryColumnNames(db)
        addColumnIfMissing(db, columns, "total_chapters", "INTEGER NOT NULL DEFAULT 0")
        addColumnIfMissing(db, columns, "is_favorite", "INTEGER NOT NULL DEFAULT 0")
        addColumnIfMissing(db, columns, "deleted_at", "INTEGER")
    }

    private fun migrateReadingProgressColumnsIfNeeded(db: SQLiteDatabase) {
        val columns = listReadingProgressColumnNames(db)
        addReadingProgressColumnIfMissing(db, columns, "scroll_range_max", "REAL NOT NULL DEFAULT 0")
    }

    private fun listReadingProgressColumnNames(db: SQLiteDatabase): MutableSet<String> {
        val set = mutableSetOf<String>()
        db.rawQuery("PRAGMA table_info($TABLE_READING_PROGRESS);", null).use { c ->
            val idx = c.getColumnIndex("name")
            while (c.moveToNext()) {
                if (idx >= 0) set.add(c.getString(idx))
            }
        }
        return set
    }

    private fun addReadingProgressColumnIfMissing(
        db: SQLiteDatabase,
        existing: MutableSet<String>,
        column: String,
        typeClause: String,
    ) {
        if (existing.contains(column)) return
        try {
            db.execSQL("ALTER TABLE $TABLE_READING_PROGRESS ADD COLUMN $column $typeClause;")
            existing.add(column)
        } catch (e: SQLException) {
            ChitalkaMirrorLog.e(TAG, "addReadingProgressColumnIfMissing($column)", e)
        }
    }

    private fun listLibraryColumnNames(db: SQLiteDatabase): MutableSet<String> {
        val set = mutableSetOf<String>()
        db.rawQuery("PRAGMA table_info($TABLE_LIBRARY_BOOKS);", null).use { c ->
            val idx = c.getColumnIndex("name")
            while (c.moveToNext()) {
                if (idx >= 0) set.add(c.getString(idx))
            }
        }
        return set
    }

    private fun addColumnIfMissing(
        db: SQLiteDatabase,
        existing: MutableSet<String>,
        column: String,
        typeClause: String,
    ) {
        if (existing.contains(column)) return
        try {
            db.execSQL("ALTER TABLE $TABLE_LIBRARY_BOOKS ADD COLUMN $column $typeClause;")
            existing.add(column)
        } catch (e: SQLException) {
            ChitalkaMirrorLog.e(TAG, "addColumnIfMissing($column)", e)
        }
    }

    private fun createIndexes(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_reading_progress_last_read
              ON $TABLE_READING_PROGRESS (last_read_timestamp DESC);
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_library_books_added
              ON $TABLE_LIBRARY_BOOKS (added_at DESC);
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_library_books_deleted
              ON $TABLE_LIBRARY_BOOKS (deleted_at);
            """.trimIndent(),
        )
    }

    companion object {
        private const val TAG = "ChitalkaStorage"
        const val DATABASE_NAME = "chitalka.db"
        private const val DB_VERSION = 1

        const val TABLE_READING_PROGRESS = "reading_progress"
        const val TABLE_LIBRARY_BOOKS = "library_books"

        /** WHERE-клауза для выборки/обновления одной книги по `book_id`. */
        const val WHERE_BOOK_ID = "book_id = ?"
    }
}
