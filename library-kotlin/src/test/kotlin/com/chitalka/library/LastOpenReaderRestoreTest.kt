package com.chitalka.library

import com.chitalka.core.types.LibraryBookRecord
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class LastOpenRestoreTestPersistence : LastOpenBookPersistence {
    private val map = mutableMapOf<String, String>()

    override suspend fun getItem(key: String): String? = map[key]

    override suspend fun setItem(key: String, value: String) {
        map[key] = value
    }

    override suspend fun removeItem(key: String) {
        map.remove(key)
    }
}

private class FakeLookup(
    private val records: Map<String, LibraryBookRecord?>,
) : LibraryBookLookup {
    override suspend fun getLibraryBook(bookId: String): LibraryBookRecord? = records[bookId]
}

class LastOpenReaderRestoreTest {

    @Test
    fun noKey_noNavigation() = runBlocking {
        val p = LastOpenRestoreTestPersistence()
        var opened: Pair<String, String>? = null
        val out =
            restoreLastOpenReaderIfNeeded(
                lookup = FakeLookup(emptyMap()),
                lastOpenPersistence = p,
                openReader = { uri, id -> opened = uri to id },
            )
        assertEquals(LastOpenReaderRestoreOutcome.NoStoredLastOpen, out)
        assertNull(opened)
    }

    @Test
    fun missingBook_clearsKey() = runBlocking {
        val p = LastOpenRestoreTestPersistence()
        setLastOpenBookId(p, "b1")
        val out =
            restoreLastOpenReaderIfNeeded(
                lookup = FakeLookup(mapOf("b1" to null)),
                lastOpenPersistence = p,
                openReader = { _, _ -> },
            )
        assertEquals(LastOpenReaderRestoreOutcome.ClearedMissingOrTrashed, out)
        assertNull(getLastOpenBookId(p))
    }

    @Test
    fun trashedBook_clearsKey() = runBlocking {
        val p = LastOpenRestoreTestPersistence()
        setLastOpenBookId(p, "b1")
        val trashed =
            LibraryBookRecord(
                bookId = "b1",
                fileUri = "file:///x.epub",
                title = "T",
                author = "A",
                fileSizeBytes = 1,
                coverUri = null,
                addedAt = 0,
                totalChapters = 0,
                isFavorite = false,
                deletedAt = 1L,
            )
        restoreLastOpenReaderIfNeeded(
            lookup = FakeLookup(mapOf("b1" to trashed)),
            lastOpenPersistence = p,
            openReader = { _, _ -> },
        )
        assertNull(getLastOpenBookId(p))
    }

    @Test
    fun activeBook_opensReader() = runBlocking {
        val p = LastOpenRestoreTestPersistence()
        setLastOpenBookId(p, "b1")
        val active =
            LibraryBookRecord(
                bookId = "b1",
                fileUri = "file:///book.epub",
                title = "T",
                author = "A",
                fileSizeBytes = 1,
                coverUri = null,
                addedAt = 0,
                totalChapters = 3,
                isFavorite = true,
                deletedAt = null,
            )
        var opened: Pair<String, String>? = null
        val out =
            restoreLastOpenReaderIfNeeded(
                lookup = FakeLookup(mapOf("b1" to active)),
                lastOpenPersistence = p,
                openReader = { uri, id -> opened = uri to id },
            )
        assertTrue(out is LastOpenReaderRestoreOutcome.Navigated)
        assertEquals("file:///book.epub" to "b1", opened)
    }
}
