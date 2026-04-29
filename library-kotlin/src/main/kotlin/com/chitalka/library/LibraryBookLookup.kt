package com.chitalka.library

import com.chitalka.core.types.LibraryBookRecord

/**
 * Минимальный контракт для автооткрытия последней книги.
 * Реализация на Android — [com.chitalka.storage.StorageService].
 */
fun interface LibraryBookLookup {
    suspend fun getLibraryBook(bookId: String): LibraryBookRecord?
}
