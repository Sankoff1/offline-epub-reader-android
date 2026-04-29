package com.chitalka.screens.common

import com.chitalka.core.types.LibraryBookWithProgress
import java.util.Locale

/** Общая фильтрация списков книг по строке поиска. */
object BookListSearchFilter {

    /** Trim + lowercase по системной локали. */
    fun normalizeBookListSearchQuery(raw: String): String =
        raw.trim().lowercase(Locale.getDefault())

    fun filterBooksByNormalizedSearchQuery(
        books: List<LibraryBookWithProgress>,
        normalizedQuery: String,
    ): List<LibraryBookWithProgress> {
        if (normalizedQuery.isEmpty()) {
            return books
        }
        val locale = Locale.getDefault()
        return books.filter { b ->
            b.record.title.lowercase(locale).contains(normalizedQuery) ||
                b.record.author.lowercase(locale).contains(normalizedQuery)
        }
    }
}
