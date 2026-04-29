package com.chitalka.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThemeColorsTest {

    @Test
    fun themeMode_fromCode() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromCode("light"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromCode("dark"))
        assertNull(ThemeMode.fromCode("LIGHT"))
    }
}
