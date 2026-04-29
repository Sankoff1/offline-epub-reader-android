package com.chitalka.ui.firstlaunch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FirstLaunchModalSpecTest {

    @Test
    fun i18nKeys_areDistinct() {
        val keys = setOf(
            FirstLaunchModalSpec.I18nKeys.MESSAGE,
            FirstLaunchModalSpec.I18nKeys.CANCEL,
            FirstLaunchModalSpec.I18nKeys.PICK_EPUB,
        )
        assertEquals(3, keys.size)
    }

    @Test
    fun i18nKeys_haveExpectedPaths() {
        assertEquals("firstLaunch.message", FirstLaunchModalSpec.I18nKeys.MESSAGE)
        assertEquals("firstLaunch.cancel", FirstLaunchModalSpec.I18nKeys.CANCEL)
        assertEquals("firstLaunch.pickEpub", FirstLaunchModalSpec.I18nKeys.PICK_EPUB)
        assertNotEquals(FirstLaunchModalSpec.I18nKeys.MESSAGE, FirstLaunchModalSpec.I18nKeys.CANCEL)
    }
}
