@file:Suppress("MatchingDeclarationName")

package com.chitalka.library

/** Результат попытки автооткрытия читалки по [LAST_OPEN_BOOK_STORAGE_KEY]. */
sealed class LastOpenReaderRestoreOutcome {
    /** Ключа нет или пусто после trim. */
    data object NoStoredLastOpen : LastOpenReaderRestoreOutcome()

    /** Запись отсутствует или в корзине — ключ очищен. */
    data object ClearedMissingOrTrashed : LastOpenReaderRestoreOutcome()

    /** Вызван [openReader] с URI и id активной книги. */
    data class Navigated(
        val bookId: String,
    ) : LastOpenReaderRestoreOutcome()
}

/**
 * Если в прошлой сессии читалка была открыта, в хранилище остался id книги — открываем её снова.
 * Если пользователь вышел в меню, ключ очищен —
 * сразу [LastOpenReaderRestoreOutcome.NoStoredLastOpen].
 */
suspend fun restoreLastOpenReaderIfNeeded(
    lookup: LibraryBookLookup,
    lastOpenPersistence: LastOpenBookPersistence,
    openReader: (fileUri: String, bookId: String) -> Unit,
): LastOpenReaderRestoreOutcome {
    val bookId = getLastOpenBookId(lastOpenPersistence)
    return when {
        bookId == null -> LastOpenReaderRestoreOutcome.NoStoredLastOpen
        else -> {
            val record = lookup.getLibraryBook(bookId)
            if (record == null || record.deletedAt != null) {
                clearLastOpenBookId(lastOpenPersistence)
                LastOpenReaderRestoreOutcome.ClearedMissingOrTrashed
            } else {
                openReader(record.fileUri, record.bookId)
                LastOpenReaderRestoreOutcome.Navigated(record.bookId)
            }
        }
    }
}
