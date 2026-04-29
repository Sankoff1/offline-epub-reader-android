package com.ncorti.kotlin.template.app.ui.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.library.LibrarySessionState
import com.chitalka.library.refreshBookCount
import com.chitalka.screens.trash.TrashScreenSpec
import com.chitalka.storage.StorageService
import com.chitalka.storage.listTrashedBooks
import com.chitalka.storage.purgeBook
import com.chitalka.storage.restoreBookFromTrash
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrashViewModel(
    private val storage: StorageService,
    private val librarySession: LibrarySessionState,
    private val onLibraryChanged: () -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(TrashUiState.Initial)
    val state: StateFlow<TrashUiState> = _state.asStateFlow()

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
        _state.update {
            it.copy(
                filteredBooks = applyFilter(it.rawBooks, normalizedQuery),
                hasActiveSearch = normalizedQuery.isNotEmpty(),
            )
        }
    }

    fun onRestore(book: LibraryBookWithProgress) {
        viewModelScope.launch {
            storage.restoreBookFromTrash(book.record.bookId)
            librarySession.bumpLibraryEpoch()
            reload()
            onLibraryChanged()
            librarySession.refreshBookCount(storage)
        }
    }

    fun onPurgeRequest(book: LibraryBookWithProgress) {
        _state.update { it.copy(pendingPurge = TrashPurgeTarget(book)) }
    }

    fun onPurgeCancel() {
        _state.update { it.copy(pendingPurge = null) }
    }

    fun onPurgeConfirm() {
        val target = _state.value.pendingPurge?.book ?: return
        _state.update { it.copy(pendingPurge = null) }
        viewModelScope.launch {
            val ok =
                runCatching {
                    deleteLibraryFilesQuiet(target)
                    storage.purgeBook(target.record.bookId)
                }.isSuccess
            if (ok) {
                librarySession.bumpLibraryEpoch()
                reload()
                onLibraryChanged()
                librarySession.refreshBookCount(storage)
            }
        }
    }

    private suspend fun reload() {
        _state.update { it.copy(isLoading = true) }
        val books = storage.listTrashedBooks()
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
        TrashScreenSpec.visibleBooksForSearch(books, normalizedQuery)

    private suspend fun deleteLibraryFilesQuiet(book: LibraryBookWithProgress) {
        withContext(Dispatchers.IO) {
            tryDeleteFileUri(book.record.fileUri)
            tryDeleteFileUri(book.record.coverUri)
        }
    }

    private fun tryDeleteFileUri(uriString: String?) {
        if (uriString.isNullOrBlank()) return
        val path = Uri.parse(uriString).path ?: return
        runCatching { File(path).delete() }
    }

    class Factory(
        private val storage: StorageService,
        private val librarySession: LibrarySessionState,
        private val onLibraryChanged: () -> Unit,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TrashViewModel::class.java)) {
                "Unknown ViewModel class: $modelClass"
            }
            return TrashViewModel(storage, librarySession, onLibraryChanged) as T
        }
    }
}
