package com.ncorti.kotlin.template.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.library.LibrarySessionState
import com.chitalka.library.refreshBookCount
import com.chitalka.screens.common.BookListSearchFilter
import com.chitalka.storage.StorageService
import com.chitalka.storage.moveBookToTrash
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val storage: StorageService,
    private val librarySession: LibrarySessionState,
    private val mode: LibraryListMode,
    private val onLibraryChanged: () -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryUiState.initial(mode))
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    private var currentNormalizedQuery: String = ""

    init {
        viewModelScope.launch { reload() }
    }

    fun onExternalRefresh() {
        viewModelScope.launch { reload() }
    }

    fun onSearchQueryChange(normalizedQuery: String) {
        if (normalizedQuery == currentNormalizedQuery) return
        currentNormalizedQuery = normalizedQuery
        _state.update { it.copy(filteredBooks = applyFilter(it.rawBooks, normalizedQuery)) }
    }

    fun onBookActionsOpen(book: LibraryBookWithProgress) {
        _state.update { it.copy(activeBookSheet = BookSheetTarget(book)) }
    }

    fun onBookActionsDismiss() {
        _state.update { it.copy(activeBookSheet = null) }
    }

    fun onToggleFavorite(book: LibraryBookWithProgress) {
        viewModelScope.launch {
            storage.setBookFavorite(book.record.bookId, !book.record.isFavorite)
            librarySession.bumpLibraryEpoch()
            _state.update { it.copy(activeBookSheet = null) }
            reload()
            onLibraryChanged()
        }
    }

    fun onMoveToTrash(book: LibraryBookWithProgress) {
        viewModelScope.launch {
            storage.moveBookToTrash(book.record.bookId)
            librarySession.bumpLibraryEpoch()
            _state.update { it.copy(activeBookSheet = null) }
            reload()
            onLibraryChanged()
            librarySession.refreshBookCount(storage)
        }
    }

    private suspend fun reload() {
        _state.update { it.copy(isLoading = true) }
        val books = storage.loadBooksForMode(mode)
        _state.update {
            it.copy(
                rawBooks = books,
                filteredBooks = applyFilter(books, currentNormalizedQuery),
                isLoading = false,
            )
        }
    }

    private fun applyFilter(
        books: List<LibraryBookWithProgress>,
        normalizedQuery: String,
    ): List<LibraryBookWithProgress> =
        BookListSearchFilter.filterBooksByNormalizedSearchQuery(books, normalizedQuery)

    class Factory(
        private val storage: StorageService,
        private val librarySession: LibrarySessionState,
        private val mode: LibraryListMode,
        private val onLibraryChanged: () -> Unit,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
                "Unknown ViewModel class: $modelClass"
            }
            return LibraryViewModel(storage, librarySession, mode, onLibraryChanged) as T
        }
    }
}
