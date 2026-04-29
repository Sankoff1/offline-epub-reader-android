package com.chitalka.core.types

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoreTypesTest {

    @Test
    fun readingProgress_dataClassEquality() {
        val a = ReadingProgress(
            bookId = "b1",
            lastChapterIndex = 2,
            scrollOffset = 120.5,
            scrollRangeMax = 400.0,
            lastReadTimestamp = 1_700_000_000_000L,
        )
        val b = a.copy(scrollOffset = 200.0)
        val c = a.copy(scrollOffset = 120.5)
        assertEquals(a, c)
        assertEquals(200.0, b.scrollOffset, 0.0)
    }

    @Test
    fun libraryBookWithProgress_nullProgressFields() {
        val record = LibraryBookRecord(
            bookId = "id",
            fileUri = "file:///x.epub",
            title = "T",
            author = "A",
            fileSizeBytes = 100L,
            coverUri = null,
            addedAt = 0L,
            totalChapters = 0,
            isFavorite = false,
            deletedAt = null,
        )
        val row = LibraryBookWithProgress(
            record = record,
            lastChapterIndex = null,
            progressFraction = null,
        )
        assertNull(row.lastChapterIndex)
        assertNull(row.progressFraction)
        assertNull(row.record.coverUri)
    }

    @Test
    fun libraryBookRecord_copyFavorite() {
        val base = LibraryBookRecord(
            bookId = "x",
            fileUri = "u",
            title = "t",
            author = "a",
            fileSizeBytes = 1L,
            coverUri = "c",
            addedAt = 1L,
            totalChapters = 10,
            isFavorite = false,
            deletedAt = null,
        )
        assertEquals(true, base.copy(isFavorite = true).isFavorite)
    }
}
