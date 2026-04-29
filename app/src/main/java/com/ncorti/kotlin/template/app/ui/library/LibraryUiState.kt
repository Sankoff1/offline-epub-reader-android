package com.ncorti.kotlin.template.app.ui.library

import androidx.compose.runtime.Immutable
import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.storage.StorageService
import com.chitalka.storage.listFavoriteBooks
import com.chitalka.storage.listLibraryBooks
import com.chitalka.storage.listRecentlyReadBooks

/** Какой именно срез библиотеки показывает экран. */
enum class LibraryListMode {
    ReadingNow,
    All,
    Favorites,
}

/** Целевая книга для открытого `ModalBottomSheet` действий. */
@Immutable
data class BookSheetTarget(val book: LibraryBookWithProgress)

/** Состояние одного экземпляра экрана библиотеки. Поиск приходит снаружи. */
@Immutable
data class LibraryUiState(
    val mode: LibraryListMode,
    val rawBooks: List<LibraryBookWithProgress>,
    val filteredBooks: List<LibraryBookWithProgress>,
    val isLoading: Boolean,
    val activeBookSheet: BookSheetTarget?,
) {
    companion object {
        fun initial(mode: LibraryListMode): LibraryUiState =
            LibraryUiState(
                mode = mode,
                rawBooks = emptyList(),
                filteredBooks = emptyList(),
                isLoading = true,
                activeBookSheet = null,
            )
    }
}

internal suspend fun StorageService.loadBooksForMode(
    mode: LibraryListMode,
): List<LibraryBookWithProgress> =
    when (mode) {
        LibraryListMode.ReadingNow -> listRecentlyReadBooks()
        LibraryListMode.All -> listLibraryBooks()
        LibraryListMode.Favorites -> listFavoriteBooks()
    }
