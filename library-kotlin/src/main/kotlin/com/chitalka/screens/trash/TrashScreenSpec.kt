package com.chitalka.screens.trash

import com.chitalka.core.types.LibraryBookWithProgress
import com.chitalka.screens.common.BookListSearchFilter
import java.util.Locale

/** Контракт экрана корзины. */
object TrashScreenSpec {

    object I18nKeys {
        const val EMPTY_LIST = "screens.cart.empty"
        const val SEARCH_NO_RESULTS = "search.noResults"
        const val RESTORE = "trash.restore"
        const val DELETE_FOREVER = "trash.deleteForever"
        const val CONFIRM_DELETE_TITLE = "trash.confirmDeleteTitle"
        const val CONFIRM_DELETE_MESSAGE = "trash.confirmDeleteMessage"
        const val COMMON_CANCEL = "common.cancel"
        const val COMMON_MB = "common.mb"
    }

    private const val LIST_BOTTOM_INSET_EXTRA_DP: Int = 16

    private const val BYTES_PER_MB: Double = 1024.0 * 1024.0

    fun visibleBooksForSearch(
        books: List<LibraryBookWithProgress>,
        normalizedQuery: String,
    ): List<LibraryBookWithProgress> =
        BookListSearchFilter.filterBooksByNormalizedSearchQuery(books, normalizedQuery)

    /** Ключ для пустого состояния: при активном поиске — `search.noResults`, иначе `screens.cart.empty`. */
    fun emptyListKey(hasActiveSearch: Boolean): String =
        if (hasActiveSearch) I18nKeys.SEARCH_NO_RESULTS else I18nKeys.EMPTY_LIST

    fun listContentBottomPaddingDp(safeInsetBottomDp: Int): Int =
        safeInsetBottomDp + LIST_BOTTOM_INSET_EXTRA_DP

    /** Числовая часть «X.XX» (без единицы) — единицу `common.mb` UI добавляет сам. */
    fun formatFileSizeMbNumber(fileSizeBytes: Long): String {
        val mb = fileSizeBytes.toDouble() / BYTES_PER_MB
        return String.format(Locale.US, "%.2f", mb)
    }
}
