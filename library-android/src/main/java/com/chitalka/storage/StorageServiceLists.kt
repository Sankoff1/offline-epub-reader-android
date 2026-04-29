package com.chitalka.storage

import com.chitalka.core.types.LibraryBookWithProgress

internal const val JOINED_SELECT_COLUMNS =
    "SELECT lb.book_id, lb.file_uri, lb.title, lb.author, lb.file_size_bytes, " +
        "lb.cover_uri, lb.added_at, lb.total_chapters, lb.is_favorite, lb.deleted_at, " +
        "rp.last_chapter_index, rp.scroll_offset, rp.scroll_range_max"

private suspend fun StorageService.queryJoined(sql: String): List<LibraryBookWithProgress> =
    withReadDb { db ->
        db.rawQuery(sql, null).use { c ->
            if (!c.moveToFirst()) return@use emptyList()
            val out = ArrayList<LibraryBookWithProgress>(c.count)
            do {
                out.add(c.mapJoinedRowByOrdinal())
            } while (c.moveToNext())
            out
        }
    }

suspend fun StorageService.listLibraryBooks(): List<LibraryBookWithProgress> =
    queryJoined(
        """
        $JOINED_SELECT_COLUMNS
        FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS} AS lb
        LEFT JOIN ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS} AS rp ON rp.book_id = lb.book_id
        WHERE lb.deleted_at IS NULL
        ORDER BY lb.added_at DESC;
        """.trimIndent(),
    )

suspend fun StorageService.listRecentlyReadBooks(): List<LibraryBookWithProgress> =
    queryJoined(
        """
        $JOINED_SELECT_COLUMNS
        FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS} AS lb
        INNER JOIN ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS} AS rp ON rp.book_id = lb.book_id
        WHERE lb.deleted_at IS NULL
        ORDER BY rp.last_read_timestamp DESC;
        """.trimIndent(),
    )

suspend fun StorageService.listFavoriteBooks(): List<LibraryBookWithProgress> =
    queryJoined(
        """
        $JOINED_SELECT_COLUMNS
        FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS} AS lb
        LEFT JOIN ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS} AS rp ON rp.book_id = lb.book_id
        WHERE lb.is_favorite = 1 AND lb.deleted_at IS NULL
        ORDER BY lb.added_at DESC;
        """.trimIndent(),
    )

suspend fun StorageService.listTrashedBooks(): List<LibraryBookWithProgress> =
    queryJoined(
        """
        $JOINED_SELECT_COLUMNS
        FROM ${ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS} AS lb
        LEFT JOIN ${ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS} AS rp ON rp.book_id = lb.book_id
        WHERE lb.deleted_at IS NOT NULL
        ORDER BY lb.deleted_at DESC;
        """.trimIndent(),
    )
