package com.chitalka.storage

import android.content.ContentValues

suspend fun StorageService.moveBookToTrash(bookId: String) {
    assertNonEmptyBookId(bookId)
    withDb { db ->
        val cv = ContentValues().apply { put("deleted_at", System.currentTimeMillis()) }
        db.update(
            ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
            cv,
            ChitalkaSqliteOpenHelper.WHERE_BOOK_ID,
            arrayOf(bookId),
        )
    }
}

suspend fun StorageService.restoreBookFromTrash(bookId: String) {
    assertNonEmptyBookId(bookId)
    withDb { db ->
        val cv = ContentValues().apply { putNull("deleted_at") }
        db.update(
            ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
            cv,
            ChitalkaSqliteOpenHelper.WHERE_BOOK_ID,
            arrayOf(bookId),
        )
    }
}

suspend fun StorageService.purgeBook(bookId: String) {
    assertNonEmptyBookId(bookId)
    withDb { db ->
        db.delete(
            ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS,
            ChitalkaSqliteOpenHelper.WHERE_BOOK_ID,
            arrayOf(bookId),
        )
        db.delete(
            ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS,
            ChitalkaSqliteOpenHelper.WHERE_BOOK_ID,
            arrayOf(bookId),
        )
    }
}

suspend fun StorageService.clearAllData() {
    withDb { db ->
        db.delete(ChitalkaSqliteOpenHelper.TABLE_READING_PROGRESS, null, null)
        db.delete(ChitalkaSqliteOpenHelper.TABLE_LIBRARY_BOOKS, null, null)
    }
}
