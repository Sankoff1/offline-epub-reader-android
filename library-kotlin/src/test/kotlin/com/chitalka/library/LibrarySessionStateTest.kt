package com.chitalka.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySessionStateTest {

    @Test
    fun bumpLibraryEpoch_increments() {
        val s = LibrarySessionState(LibrarySessionUiState(libraryEpoch = 2))
        s.bumpLibraryEpoch()
        assertEquals(3, s.state.value.libraryEpoch)
    }

    @Test
    fun search_openCloseClearQuery() {
        val s = LibrarySessionState()
        s.openSearch()
        assertTrue(s.state.value.isSearchOpen)
        s.setSearchQuery("foo")
        assertEquals("foo", s.state.value.searchQuery)
        s.closeSearch()
        assertFalse(s.state.value.isSearchOpen)
        assertEquals("", s.state.value.searchQuery)
    }

    @Test
    fun welcomeVisible_requiresStorageReadyAndEmptyAndNotDismissed() {
        val s = LibrarySessionState()
        assertFalse(s.state.value.isFirstLaunchWelcomeVisible)
        s.markStorageReady(true)
        assertTrue(s.state.value.isFirstLaunchWelcomeVisible)
        s.dismissWelcomeModal()
        assertFalse(s.state.value.isFirstLaunchWelcomeVisible)
    }

    @Test
    fun welcomeHiddenWhenBooksOrSuppressed() {
        val s = LibrarySessionState(LibrarySessionUiState(bookCount = 1))
        s.markStorageReady(true)
        assertFalse(s.state.value.isFirstLaunchWelcomeVisible)

        val empty = LibrarySessionState()
        empty.markStorageReady(true)
        empty.setSuppressWelcomeForPicker(true)
        assertFalse(empty.state.value.isFirstLaunchWelcomeVisible)
    }

    @Test
    fun dismissWelcome_clearsHint() {
        val s = LibrarySessionState()
        s.setWelcomePickerHint("err")
        assertEquals("err", s.state.value.welcomePickerHint)
        s.dismissWelcomeModal()
        assertNull(s.state.value.welcomePickerHint)
    }

    @Test
    fun updateBookCount_clampsToIntRange() {
        val s = LibrarySessionState()
        s.updateBookCount(Long.MAX_VALUE)
        assertEquals(Int.MAX_VALUE, s.state.value.bookCount)
        s.updateBookCount(-1)
        assertEquals(0, s.state.value.bookCount)
    }
}
