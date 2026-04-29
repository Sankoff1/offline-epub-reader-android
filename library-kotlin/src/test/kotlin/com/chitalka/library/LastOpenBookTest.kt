package com.chitalka.library

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

class LastOpenBookTest {

    @Test
    fun get_returnsNullForBlankOrWhitespace() = runBlocking {
        val p = InMemoryPersistence()
        p.setItem(LAST_OPEN_BOOK_STORAGE_KEY, "   ")
        assertNull(getLastOpenBookId(p))
        p.setItem(LAST_OPEN_BOOK_STORAGE_KEY, "")
        assertNull(getLastOpenBookId(p))
    }

    @Test
    fun set_get_clear_roundTrip() = runBlocking {
        val p = InMemoryPersistence()
        setLastOpenBookId(p, "  book-1  ")
        assertEquals("  book-1  ", getLastOpenBookId(p))
        clearLastOpenBookId(p)
        assertNull(getLastOpenBookId(p))
    }

    @Test
    fun set_blank_isNoOp() = runBlocking {
        val p = InMemoryPersistence()
        p.setItem(LAST_OPEN_BOOK_STORAGE_KEY, "x")
        setLastOpenBookId(p, "   ")
        assertEquals("x", getLastOpenBookId(p))
    }
}
