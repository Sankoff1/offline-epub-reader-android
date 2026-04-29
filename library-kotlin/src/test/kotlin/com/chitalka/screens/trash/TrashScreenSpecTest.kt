package com.chitalka.screens.trash

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrashScreenSpecTest {

    @Test
    fun emptyListKey_picksByActiveSearch() {
        assertEquals(TrashScreenSpec.I18nKeys.EMPTY_LIST, TrashScreenSpec.emptyListKey(false))
        assertEquals(TrashScreenSpec.I18nKeys.SEARCH_NO_RESULTS, TrashScreenSpec.emptyListKey(true))
    }

    @Test
    fun listContentBottomPadding() {
        assertEquals(32, TrashScreenSpec.listContentBottomPaddingDp(16))
    }

    @Test
    fun formatFileSizeMbNumber() {
        val number = TrashScreenSpec.formatFileSizeMbNumber(2_097_152L)
        assertTrue(number.startsWith("2.00"))
    }
}
