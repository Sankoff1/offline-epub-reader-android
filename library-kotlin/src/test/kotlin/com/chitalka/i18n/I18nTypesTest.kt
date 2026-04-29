package com.chitalka.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class I18nTypesTest {

    @Test
    fun appLocales_orderMatchesTypeScript() {
        assertEquals(listOf("ru", "en"), APP_LOCALES.map { it.code })
    }

    @Test
    fun fromCode_roundTrip() {
        assertEquals(AppLocale.RU, AppLocale.fromCode("ru"))
        assertEquals(AppLocale.EN, AppLocale.fromCode("en"))
        assertNull(AppLocale.fromCode("de"))
        assertNull(AppLocale.fromCode("RU"))
    }

    @Test
    fun localeStorageKey() {
        assertEquals("chitalka_locale", LOCALE_STORAGE_KEY)
    }
}
