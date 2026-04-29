package com.chitalka.storage

import android.database.Cursor
import com.chitalka.core.types.LibraryBookRecord
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.core.types.ReadingProgress
import com.chitalka.library.libraryListProgressFraction
import kotlin.math.max

internal fun Cursor.mapLibraryBookRecordByOrdinal(): LibraryBookRecord {
    val totalChapters = max(0, getInt(7))
    val deletedAt = if (isNull(9)) null else getLong(9)
    return LibraryBookRecord(
        bookId = getString(0),
        fileUri = getString(1),
        title = getString(2),
        author = getString(3),
        fileSizeBytes = getLong(4),
        coverUri = if (isNull(5)) null else getString(5),
        addedAt = getLong(6),
        totalChapters = totalChapters,
        isFavorite = getInt(8) != 0,
        deletedAt = deletedAt,
    )
}

internal fun Cursor.mapJoinedRowByOrdinal(): LibraryBookWithProgress {
    val record = mapLibraryBookRecordByOrdinal()
    val lastChapterIndex: Int? = if (isNull(10)) null else max(0, getInt(10))
    val scrollOffset =
        if (lastChapterIndex == null || isNull(11)) {
            0.0
        } else {
            getDouble(11)
        }
    val scrollRangeMax =
        if (lastChapterIndex == null || isNull(12)) {
            0.0
        } else {
            getDouble(12)
        }
    val progressFraction: Double? = lastChapterIndex?.let { idx ->
        libraryListProgressFraction(
            totalChapters = record.totalChapters,
            lastChapterIndex = idx,
            scrollOffset = scrollOffset,
            scrollRangeMax = scrollRangeMax,
        )
    }
    return LibraryBookWithProgress(
        record = record,
        lastChapterIndex = lastChapterIndex,
        progressFraction = progressFraction,
    )
}

internal fun assertNonEmptyBookId(bookId: String) {
    if (bookId.isBlank()) {
        throw StorageServiceError(STORAGE_ERR_INVALID_BOOK_ID)
    }
}

internal fun assertValidProgress(progress: ReadingProgress) {
    assertNonEmptyBookId(progress.bookId)
    if (!progress.scrollOffset.isFinite()) {
        throw StorageServiceError(STORAGE_ERR_INVALID_PROGRESS_OFFSET)
    }
    if (!progress.scrollRangeMax.isFinite() || progress.scrollRangeMax < 0.0) {
        throw StorageServiceError(STORAGE_ERR_INVALID_PROGRESS_SCROLL_RANGE)
    }
}
