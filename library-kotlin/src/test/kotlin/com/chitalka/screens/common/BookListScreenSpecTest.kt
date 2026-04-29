package com.chitalka.screens.common

import com.chitalka.core.types.LibraryBookWithProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BookListScreenSpecTest {

    private fun book(id: String, title: String, author: String): LibraryBookWithProgress =
        LibraryBookWithProgress(
            record = com.chitalka.core.types.LibraryBookRecord(
                bookId = id,
                fileUri = "file:///$id.epub",
                title = title,
                author = author,
                fileSizeBytes = 0L,
                coverUri = null,
                addedAt = 0L,
                totalChapters = 0,
                isFavorite = false,
                deletedAt = null,
            ),
            lastChapterIndex = null,
            progressFraction = null,
        )

    @Test
    fun visibleBooksForSearch_filtersByTitleAndAuthor() {
        val books = listOf(
            book("1", "Alpha", "Doe"),
            book("2", "Beta", "Smith"),
            book("3", "Gamma", "Doe"),
        )
        val q = BookListScreenSpec.BooksAndDocs.normalizeSearchQuery("doe")
        val out = BookListScreenSpec.BooksAndDocs.visibleBooksForSearch(books, q)
        assertEquals(listOf("1", "3"), out.map { it.record.bookId })
    }

    @Test
    fun visibleBooksForSearch_emptyQueryReturnsAll() {
        val books = listOf(book("1", "A", "X"))
        assertEquals(books, BookListScreenSpec.ReadingNow.visibleBooksForSearch(books, ""))
    }

    @Test
    fun emptyListKey_picksSearchKeyOnActiveSearch() {
        val withSearch = BookListScreenSpec.Favorites.emptyListKey(hasActiveSearch = true)
        val withoutSearch = BookListScreenSpec.Favorites.emptyListKey(hasActiveSearch = false)
        assertEquals(BookListScreenSpec.SEARCH_NO_RESULTS_KEY, withSearch)
        assertEquals(BookListScreenSpec.Favorites.emptyI18nKey, withoutSearch)
        assertNotEquals(withSearch, withoutSearch)
    }

    @Test
    fun listContentBottomPaddingDp_reservesFabSpaceForFabScreens() {
        val safe = 16
        val withFab = BookListScreenSpec.BooksAndDocs.listContentBottomPaddingDp(safe)
        val withoutFab = BookListScreenSpec.Favorites.listContentBottomPaddingDp(safe)
        assertEquals(BookListScreenLayout.listContentBottomPaddingDp(safe), withFab)
        assertEquals(safe + 16, withoutFab)
    }

    @Test
    fun variants_haveDistinctEmptyKeys() {
        val keys = setOf(
            BookListScreenSpec.ReadingNow.emptyI18nKey,
            BookListScreenSpec.BooksAndDocs.emptyI18nKey,
            BookListScreenSpec.Favorites.emptyI18nKey,
        )
        assertEquals(3, keys.size)
    }

    @Test
    fun favoritesHasNoFab_othersHaveFab() {
        assertEquals(false, BookListScreenSpec.Favorites.hasFab)
        assertEquals(true, BookListScreenSpec.ReadingNow.hasFab)
        assertEquals(true, BookListScreenSpec.BooksAndDocs.hasFab)
    }
}
