package com.chitalka.i18n

import com.chitalka.library.LastOpenBookPersistence
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private class InMemoryPersistence : LastOpenBookPersistence {
    private val map = mutableMapOf<String, String>()

    override suspend fun getItem(key: String): String? = map[key]

    override suspend fun setItem(key: String, value: String) {
        map[key] = value
    }

    override suspend fun removeItem(key: String) {
        map.remove(key)
    }
}

class I18nPreferencesTest {

    @Test
    fun loadPersistedLocale_nullWhenMissing() = runBlocking {
        assertNull(loadPersistedLocale(InMemoryPersistence()))
    }

    @Test
    fun loadPersistedLocale_parsesRuEn() = runBlocking {
        val p = InMemoryPersistence()
        p.setItem(LOCALE_STORAGE_KEY, "ru")
        assertEquals(AppLocale.RU, loadPersistedLocale(p))
        p.setItem(LOCALE_STORAGE_KEY, "en")
        assertEquals(AppLocale.EN, loadPersistedLocale(p))
    }

    @Test
    fun loadPersistedLocale_invalidReturnsNull() = runBlocking {
        val p = InMemoryPersistence()
        p.setItem(LOCALE_STORAGE_KEY, "RU")
        assertNull(loadPersistedLocale(p))
    }

    @Test
    fun persist_roundTrip() = runBlocking {
        val p = InMemoryPersistence()
        persistLocale(p, AppLocale.EN)
        assertEquals(AppLocale.EN, loadPersistedLocale(p))
    }
}
