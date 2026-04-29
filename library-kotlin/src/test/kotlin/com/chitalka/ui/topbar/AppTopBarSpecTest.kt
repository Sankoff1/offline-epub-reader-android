package com.chitalka.ui.topbar

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppTopBarSpecTest {

    @Test
    fun searchButton_visibleOnlyOnSearchableRouteWithBooks() {
        val open = AppTopBarSpec.SearchChromeState(bookCount = 1, isSearchOpen = false, searchQuery = "")
        assertTrue(AppTopBarSpec.shouldShowSearchButton("ReadingNow", open))
        assertFalse(AppTopBarSpec.shouldShowSearchButton("Settings", open))
        assertFalse(AppTopBarSpec.shouldShowSearchButton("ReadingNow", open.copy(bookCount = 0)))
        assertFalse(AppTopBarSpec.shouldShowSearchButton("ReadingNow", open.copy(isSearchOpen = true)))
    }

    @Test
    fun searchInput_whenOpenAndSearchable() {
        val s = AppTopBarSpec.SearchChromeState(bookCount = 0, isSearchOpen = true, searchQuery = "x")
        assertTrue(AppTopBarSpec.shouldShowSearchInput("BooksAndDocs", s))
        assertFalse(AppTopBarSpec.shouldShowSearchInput("DebugLogs", s))
    }

    @Test
    fun clearQuery_whenInputVisibleAndNonEmptyQuery() {
        val s = AppTopBarSpec.SearchChromeState(bookCount = 1, isSearchOpen = true, searchQuery = "ab")
        assertTrue(AppTopBarSpec.shouldShowClearQueryButton("Favorites", s))
        assertFalse(AppTopBarSpec.shouldShowClearQueryButton("Favorites", s.copy(searchQuery = "")))
    }

    @Test
    fun autoCloseSearch_whenLeavingSearchableStack() {
        assertTrue(AppTopBarSpec.shouldAutoCloseSearchForRoute("Settings", isSearchOpen = true))
        assertFalse(AppTopBarSpec.shouldAutoCloseSearchForRoute("Settings", isSearchOpen = false))
        assertFalse(AppTopBarSpec.shouldAutoCloseSearchForRoute("ReadingNow", isSearchOpen = true))
    }
}
