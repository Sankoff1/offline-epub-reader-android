package com.chitalka.core.types

/**
 * Сохранённая позиция чтения одной книги. Хранится в SQLite, ключ — `book_id`.
 */
data class ReadingProgress(
    val bookId: String,
    val lastChapterIndex: Int,
    val scrollOffset: Double,
    /** Макс. прокрутка по текущей главе (scrollHeight − viewport), 0 если ещё не известна. */
    val scrollRangeMax: Double,
    val lastReadTimestamp: Long,
)
