package com.chitalka.screens.settings

import com.chitalka.i18n.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsScreenSpecTest {

    @Test
    fun shouldOpenLanguageMenuAbove() {
        val anchorY = 400f
        val anchorH = 48f
        val winTall = 800f
        assertFalse(SettingsScreenSpec.shouldOpenLanguageMenuAbove(winTall, anchorY, anchorH))
        val winShort = 500f
        assertTrue(SettingsScreenSpec.shouldOpenLanguageMenuAbove(winShort, anchorY, anchorH))
    }

    @Test
    fun languageMenuTopPx() {
        assertEquals(447f, SettingsScreenSpec.languageMenuTopPx(400f, 48f, openAbove = false), 0f)
        assertEquals(304f, SettingsScreenSpec.languageMenuTopPx(400f, 48f, openAbove = true), 0f)
    }

    @Test
    fun languageOptionKey_picksByLocale() {
        assertEquals(SettingsScreenSpec.I18nKeys.LANGUAGE_RU, SettingsScreenSpec.languageOptionKey(AppLocale.RU))
        assertEquals(SettingsScreenSpec.I18nKeys.LANGUAGE_EN, SettingsScreenSpec.languageOptionKey(AppLocale.EN))
    }
}
