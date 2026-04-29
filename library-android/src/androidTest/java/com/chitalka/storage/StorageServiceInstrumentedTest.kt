package com.chitalka.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chitalka.core.types.LibraryBookRecord
import com.chitalka.core.types.ReadingProgress
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StorageServiceInstrumentedTest {

    private lateinit var storage: StorageService

    @Before
    fun setup() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        ctx.deleteDatabase(ChitalkaSqliteOpenHelper.DATABASE_NAME)
        storage = StorageService(ctx)
    }

    @Test
    fun progress_roundTrip(): Unit = runBlocking {
        val p = ReadingProgress("b1", 2, 10.0, 100.0, 99L)
        storage.saveProgress(p)
        assertEquals(p, storage.getProgress("b1"))
    }

    @Test
    fun upsert_clearsDeletedAt(): Unit = runBlocking {
        val row =
            LibraryBookRecord(
                bookId = "x",
                fileUri = "file:///a.epub",
                title = "T",
                author = "A",
                fileSizeBytes = 1L,
                coverUri = null,
                addedAt = 1L,
                totalChapters = 0,
                isFavorite = false,
                deletedAt = 100L,
            )
        storage.upsertLibraryBook(row)
        storage.moveBookToTrash("x")
        assertNotNull(storage.getLibraryBook("x")?.deletedAt)
        storage.upsertLibraryBook(row.copy(deletedAt = null))
        assertNull(storage.getLibraryBook("x")?.deletedAt)
    }

    @Test
    fun listRecentlyRead_ordersByLastRead(): Unit = runBlocking {
        storage.upsertLibraryBook(
            LibraryBookRecord(
                bookId = "a",
                fileUri = "f1",
                title = "t1",
                author = "a1",
                fileSizeBytes = 1L,
                coverUri = null,
                addedAt = 1L,
                totalChapters = 10,
                isFavorite = false,
                deletedAt = null,
            ),
        )
        storage.upsertLibraryBook(
            LibraryBookRecord(
                bookId = "b",
                fileUri = "f2",
                title = "t2",
                author = "a2",
                fileSizeBytes = 1L,
                coverUri = null,
                addedAt = 2L,
                totalChapters = 10,
                isFavorite = false,
                deletedAt = null,
            ),
        )
        storage.saveProgress(ReadingProgress("a", 0, 0.0, 0.0, 100L))
        storage.saveProgress(ReadingProgress("b", 0, 0.0, 0.0, 200L))
        val list = storage.listRecentlyReadBooks()
        assertEquals(2, list.size)
        assertEquals("b", list[0].bookId)
        assertEquals("a", list[1].bookId)
    }

    @Test
    fun progressFraction_whenTotalChaptersKnown(): Unit = runBlocking {
        storage.upsertLibraryBook(
            LibraryBookRecord(
                bookId = "p",
                fileUri = "f",
                title = "t",
                author = "a",
                fileSizeBytes = 1L,
                coverUri = null,
                addedAt = 1L,
                totalChapters = 10,
                isFavorite = false,
                deletedAt = null,
            ),
        )
        storage.saveProgress(ReadingProgress("p", 4, 500.0, 500.0, 1L))
        val item = storage.listLibraryBooks().single()
        assertEquals(4, item.lastChapterIndex)
        assertNotNull(item.progressFraction)
        assertEquals(0.5, item.progressFraction!!, 0.001)
    }

    @Test
    fun purge_removesBookAndProgress(): Unit = runBlocking {
        storage.upsertLibraryBook(
            LibraryBookRecord(
                bookId = "z",
                fileUri = "f",
                title = "t",
                author = "a",
                fileSizeBytes = 1L,
                coverUri = null,
                addedAt = 1L,
                totalChapters = 0,
                isFavorite = false,
                deletedAt = null,
            ),
        )
        storage.saveProgress(ReadingProgress("z", 0, 0.0, 0.0, 1L))
        storage.purgeBook("z")
        assertNull(storage.getLibraryBook("z"))
        assertNull(storage.getProgress("z"))
    }

    @Test
    fun countLibraryBooks_excludesTrash(): Unit = runBlocking {
        storage.upsertLibraryBook(
            LibraryBookRecord(
                bookId = "t1",
                fileUri = "f",
                title = "t",
                author = "a",
                fileSizeBytes = 1L,
                coverUri = null,
                addedAt = 1L,
                totalChapters = 0,
                isFavorite = false,
                deletedAt = null,
            ),
        )
        assertEquals(1L, storage.countLibraryBooks())
        storage.moveBookToTrash("t1")
        assertEquals(0L, storage.countLibraryBooks())
    }

    @Test
    fun setBookFavorite(): Unit = runBlocking {
        storage.upsertLibraryBook(
            LibraryBookRecord(
                bookId = "f",
                fileUri = "f",
                title = "t",
                author = "a",
                fileSizeBytes = 1L,
                coverUri = null,
                addedAt = 1L,
                totalChapters = 0,
                isFavorite = false,
                deletedAt = null,
            ),
        )
        storage.setBookFavorite("f", true)
        assertTrue(storage.getLibraryBook("f")!!.isFavorite)
    }
}
