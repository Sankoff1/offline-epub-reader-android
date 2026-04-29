package com.chitalka.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibraryListProgressFractionTest {

    @Test
    fun null_whenNoChapters() {
        assertNull(libraryListProgressFraction(0, 0, 0.0, 100.0))
    }

    @Test
    fun startOfFirstChapter() {
        assertEquals(0.0, libraryListProgressFraction(10, 0, 0.0, 500.0)!!, 1e-9)
    }

    @Test
    fun halfOfFourthChapter_ofTen() {
        assertEquals(0.45, libraryListProgressFraction(10, 4, 250.0, 500.0)!!, 1e-9)
    }

    @Test
    fun endOfFourthChapter_ofTen() {
        assertEquals(0.5, libraryListProgressFraction(10, 4, 500.0, 500.0)!!, 1e-9)
    }

    @Test
    fun unknownScrollRange_countsAsChapterStart() {
        assertEquals(0.4, libraryListProgressFraction(10, 4, 250.0, 0.0)!!, 1e-9)
    }

    @Test
    fun clampsChapterIndex() {
        assertEquals(2.0 / 3.0, libraryListProgressFraction(3, 99, 0.0, 1.0)!!, 1e-9)
    }

    @Test
    fun endOfLastChapter() {
        assertEquals(1.0, libraryListProgressFraction(3, 2, 1.0, 1.0)!!, 1e-9)
    }
}
