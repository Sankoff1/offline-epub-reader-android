package com.chitalka.screens.common

import com.chitalka.core.types.LibraryBookWithProgress

/**
 * Контракт списка книг для экранов «Сейчас читаю», «Книги и документы», «Избранное».
 * Различия между экранами — только i18n-ключ пустого состояния и наличие FAB.
 */
data class BookListScreenSpec(
    val emptyI18nKey: String,
    val hasFab: Boolean,
) {
    fun normalizeSearchQuery(raw: String): String =
        BookListSearchFilter.normalizeBookListSearchQuery(raw)

    fun visibleBooksForSearch(
        books: List<LibraryBookWithProgress>,
        normalizedQuery: String,
    ): List<LibraryBookWithProgress> =
        BookListSearchFilter.filterBooksByNormalizedSearchQuery(books, normalizedQuery)

    /** Ключ для пустого состояния: при активном поиске — `search.noResults`, иначе `emptyI18nKey`. */
    fun emptyListKey(hasActiveSearch: Boolean): String =
        if (hasActiveSearch) SEARCH_NO_RESULTS_KEY else emptyI18nKey

    /**
     * Нижний padding списка. Для экранов с FAB резервирует место под кнопку и зазор;
     * для экранов без FAB — только safe-area + базовый отступ.
     */
    fun listContentBottomPaddingDp(safeInsetBottomDp: Int): Int =
        if (hasFab) {
            BookListScreenLayout.listContentBottomPaddingDp(safeInsetBottomDp)
        } else {
            safeInsetBottomDp + LIST_BOTTOM_INSET_NO_FAB_DP
        }

    companion object {
        const val SEARCH_NO_RESULTS_KEY: String = "search.noResults"

        /** Дефолтный i18n-ключ пустого состояния «Книги и документы». */
        const val BOOKS_EMPTY_KEY: String = "books.empty"

        private const val LIST_BOTTOM_INSET_NO_FAB_DP: Int = 16

        val ReadingNow: BookListScreenSpec =
            BookListScreenSpec(emptyI18nKey = "screens.readingNow.subtitle", hasFab = true)

        val BooksAndDocs: BookListScreenSpec =
            BookListScreenSpec(emptyI18nKey = BOOKS_EMPTY_KEY, hasFab = true)

        val Favorites: BookListScreenSpec =
            BookListScreenSpec(emptyI18nKey = "screens.favorites.empty", hasFab = false)
    }
}
