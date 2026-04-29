package com.chitalka.storage

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.chitalka.core.types.LibraryBookRecord
import com.chitalka.core.types.ReadingProgress
import com.chitalka.debug.ChitalkaMirrorLog
import com.chitalka.library.LibraryBookLookup
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val LOG_PREFIX = "[StorageService]"
private const val STORAGE_LOG_TAG = "ChitalkaStorage"

/** SQLite-хранилище прогресса чтения и записей библиотеки. */
@Suppress("TooManyFunctions")
class StorageService internal constructor(
    private val helper: ChitalkaSqliteOpenHelper,
) : LibraryBookLookup {
    constructor(context: Context) : this(ChitalkaSqliteOpenHelper(context))

    internal suspend fun <T> withDb(block: (SQLiteDatabase) -> T): T =
        withContext(Dispatchers.IO) {
            try {
                block(helper.writableDatabase)
            } catch (e: SQLException) {
                throw mapDbException(e)
            }
        }

    internal suspend fun <T> withReadDb(block: (SQLiteDatabase) -> T): T =
        withContext(Dispatchers.IO) {
            try {
                block(helper.readableDatabase)
            } catch (e: SQLException) {
                throw mapDbException(e)
            }
        }

    private fun mapDbException(e: SQLException): StorageServiceError {
        ChitalkaMirrorLog.e(STORAGE_LOG_TAG, "$LOG_PREFIX ${e.message}", e)
        val msg = e.message.orEmpty()
        val openHints = listOf("unable to open", "Could not open", "disk I/O", "SQLITE_CANTOPEN")
        val isLikelyOpen = openHints.any { msg.contains(it, ignoreCase = true) }
        val code = if (isLikelyOpen) STORAGE_ERR_OPEN_FAILED else STORAGE_ERR_GENERIC
        return StorageServiceError(code, e)
    }

    suspend fun saveProgress(progress: ReadingProgress) {
        assertValidProgress(progress)
        withDb { db ->
            val cv =
                ContentValues().apply {
                    put("book_id", progress.bookId)
                    put("last_chapter_index", progress.lastChapterIndex)
                    put("scroll_offset", progress.scrollOffset)
                    put("scroll_range_max", progress.scrollRangeMax)
                    put("last_read_timestamp", progress.lastReadTimestamp)
                }
            db.insertWithOnConflict(
                ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS,
                null,
                cv,
                SQLiteDatabase.CONFLICT_REPLACE,
            )
        }
    }

    suspend fun getProgress(bookId: String): ReadingProgress? {
        assertNonEmptyBookId(bookId)
        return withReadDb { db ->
            db.rawQuery(
                """
                SELECT
                  book_id,
                  last_chapter_index,
                  scroll_offset,
                  scroll_range_max,
                  last_read_timestamp
                FROM ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS}
                WHERE book_id = ?
                LIMIT 1;
                """.trimIndent(),
                arrayOf(bookId),
            ).use { c ->
                if (!c.moveToFirst()) {
                    null
                } else {
                    ReadingProgress(
                        bookId = c.getString(0),
                        lastChapterIndex = c.getInt(1),
                        scrollOffset = c.getDouble(2),
                        scrollRangeMax = c.getDouble(3),
                        lastReadTimestamp = c.getLong(4),
                    )
                }
            }
        }
    }

    suspend fun addBook(row: LibraryBookRecord) {
        upsertLibraryBook(row)
    }

    suspend fun upsertLibraryBook(row: LibraryBookRecord) {
        assertNonEmptyBookId(row.bookId)
        withDb { db ->
            db.beginTransactionNonExclusive()
            try {
                val updateCv =
                    ContentValues().apply {
                        put("file_uri", row.fileUri)
                        put("title", row.title)
                        put("author", row.author)
                        put("file_size_bytes", max(0L, row.fileSizeBytes))
                        if (row.coverUri != null) {
                            put("cover_uri", row.coverUri)
                        } else {
                            putNull("cover_uri")
                        }
                        put("added_at", row.addedAt)
                        putNull("deleted_at")
                    }
                val updated =
                    db.update(
                        ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
                        updateCv,
                        ChitalkaSqliteOpenHelper.WHERE_BOOK_ID,
                        arrayOf(row.bookId),
                    )
                if (updated == 0) {
                    val insertCv =
                        ContentValues().apply {
                            put("book_id", row.bookId)
                            put("file_uri", row.fileUri)
                            put("title", row.title)
                            put("author", row.author)
                            put("file_size_bytes", max(0L, row.fileSizeBytes))
                            if (row.coverUri != null) {
                                put("cover_uri", row.coverUri)
                            } else {
                                putNull("cover_uri")
                            }
                            put("added_at", row.addedAt)
                            put("total_chapters", max(0, row.totalChapters))
                            put("is_favorite", if (row.isFavorite) 1 else 0)
                            if (row.deletedAt != null) {
                                put("deleted_at", row.deletedAt)
                            } else {
                                putNull("deleted_at")
                            }
                        }
                    db.insert(ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS, null, insertCv)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    suspend fun setBookTotalChapters(bookId: String, totalChapters: Int) {
        assertNonEmptyBookId(bookId)
        val normalized = max(0, totalChapters)
        withDb { db ->
            val cv = ContentValues().apply { put("total_chapters", normalized) }
            db.update(
                ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
                cv,
                ChitalkaSqliteOpenHelper.WHERE_BOOK_ID,
                arrayOf(bookId),
            )
        }
    }

    override suspend fun getLibraryBook(bookId: String): LibraryBookRecord? {
        assertNonEmptyBookId(bookId)
        return withReadDb { db ->
            db.rawQuery(
                """
                SELECT
                  book_id,
                  file_uri,
                  title,
                  author,
                  file_size_bytes,
                  cover_uri,
                  added_at,
                  total_chapters,
                  is_favorite,
                  deleted_at
                FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS}
                WHERE book_id = ?
                LIMIT 1;
                """.trimIndent(),
                arrayOf(bookId),
            ).use { c ->
                if (!c.moveToFirst()) null else c.mapLibraryBookRecordByOrdinal()
            }
        }
    }

    suspend fun setBookFavorite(bookId: String, isFavorite: Boolean) {
        assertNonEmptyBookId(bookId)
        withDb { db ->
            val cv = ContentValues().apply { put("is_favorite", if (isFavorite) 1 else 0) }
            db.update(
                ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
                cv,
                ChitalkaSqliteOpenHelper.WHERE_BOOK_ID,
                arrayOf(bookId),
            )
        }
    }

    suspend fun countLibraryBooks(): Long =
        withReadDb { db ->
            db.rawQuery(
                """
                SELECT COUNT(*) FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS}
                WHERE deleted_at IS NULL;
                """.trimIndent(),
                null,
            ).use { c ->
                if (!c.moveToFirst()) 0L else c.getLong(0)
            }
        }

    suspend fun countBooksWithProgress(): Long =
        withReadDb { db ->
            db.rawQuery(
                """
                SELECT COUNT(*) FROM ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS};
                """.trimIndent(),
                null,
            ).use { c ->
                if (!c.moveToFirst()) 0L else c.getLong(0)
            }
        }
}
