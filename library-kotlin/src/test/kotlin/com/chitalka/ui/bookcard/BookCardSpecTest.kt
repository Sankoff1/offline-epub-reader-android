package com.chitalka.ui.bookcard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookCardSpecTest {

    @Test
    fun clampProgressFraction_clampsAndHandlesNan() {
        assertEquals(0.0, BookCardSpec.clampProgressFraction(Double.NaN), 0.0)
        assertEquals(0.0, BookCardSpec.clampProgressFraction(Double.POSITIVE_INFINITY), 0.0)
        assertEquals(0.0, BookCardSpec.clampProgressFraction(-0.5), 0.0)
        assertEquals(1.0, BookCardSpec.clampProgressFraction(2.0), 0.0)
        assertEquals(0.35, BookCardSpec.clampProgressFraction(0.35), 1e-9)
    }

    @Test
    fun hasProgressValue_onlyForNonNull() {
        assertFalse(BookCardSpec.hasProgressValue(null))
        assertTrue(BookCardSpec.hasProgressValue(Double.NaN))
    }

    @Test
    fun progressPercentRounded() {
        assertEquals(0, BookCardSpec.progressPercentRounded(Double.NaN))
        assertEquals(34, BookCardSpec.progressPercentRounded(0.335))
    }
}
