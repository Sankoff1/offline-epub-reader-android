package com.chitalka.picker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EpubPickerUtilsTest {

    @Test
    fun deriveBookId_stripsPathAndExt() {
        assertEquals("My Book", deriveBookId("/storage/My Book.epub"))
        assertEquals("x", deriveBookId("C:/docs/x.epub"))
    }

    @Test
    fun deriveBookId_emptyFallsBackToPrefix() {
        val id = deriveBookId(".epub")
        assertTrue(id.startsWith("book_"))
    }

    @Test
    fun isEpubFileName() {
        assertTrue(isEpubFileName("a.EPUB"))
        assertFalse(isEpubFileName("a.pdf"))
    }

    @Test
    fun isLikelyEpubAsset_byMime() {
        assertTrue(isLikelyEpubAsset("x.bin", "application/epub+zip", "content://a/1"))
    }

    @Test
    fun isLikelyEpubAsset_byUriPath() {
        assertTrue(isLikelyEpubAsset("", null, "content://pkg/book.epub?token=1"))
    }
}
