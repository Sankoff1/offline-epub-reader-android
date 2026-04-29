package com.chitalka.navigation

import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.library.getLastOpenBookId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private class NavTestPersistence : LastOpenBookPersistence {
    private val map = mutableMapOf<String, String>()

    override suspend fun getItem(key: String): String? = map[key]

    override suspend fun setItem(key: String, value: String) {
        map[key] = value
    }

    override suspend fun removeItem(key: String) {
        map.remove(key)
    }
}

class ReaderRouteLifecycleTest {

    @Test
    fun onReaderEntered_thenBeforeRemove_clearsKey() = runBlocking {
        val p = NavTestPersistence()
        ReaderRouteLifecycle.onReaderEntered(p, "book-1")
        assertEquals("book-1", getLastOpenBookId(p))
        ReaderRouteLifecycle.onReaderStackBeforeRemove(p)
        assertNull(getLastOpenBookId(p))
    }

    @Test
    fun onBackToLibrary_refreshesThenNavigates() = runBlocking {
        val order = mutableListOf<String>()
        ReaderRouteLifecycle.onBackToLibrary(
            refreshBookCount = {
                order.add("refresh")
            },
            goBack = { order.add("back") },
        )
        assertEquals(listOf("refresh", "back"), order)
    }

    @Test
    fun onReaderContentOpened_onlyRefresh() = runBlocking {
        var n = 0
        ReaderRouteLifecycle.onReaderContentOpened { n++ }
        assertEquals(1, n)
    }
}
