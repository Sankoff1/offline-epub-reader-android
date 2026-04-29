package com.chitalka.epub

import org.junit.Assert.assertEquals
import org.junit.Test

class EpubUriUtilsTest {

    @Test
    fun ensureFileUri_normalizesFileUriWithSingleSlashAfterScheme_windowsDrive() {
        val bad = "file:/C:/Temp/book/OEBPS/content.opf"
        val fixed = ensureFileUri(bad)
        assertEquals("file:///C:/Temp/book/OEBPS/content.opf", fixed)
    }

    @Test
    fun ensureFileUri_leavesTripleSlashUnchanged() {
        val ok = "file:///C:/Temp/book_cache/uuid/META-INF/container.xml"
        assertEquals(ok, ensureFileUri(ok))
    }
}
