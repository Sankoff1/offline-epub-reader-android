package com.chitalka.core.types

/**
 * Запись библиотеки + прогресс чтения для списочных экранов.
 * Композиция вместо дубля полей: книга остаётся в [record], прогресс — в собственных полях.
 */
data class LibraryBookWithProgress(
    val record: LibraryBookRecord,
    /** Индекс последней открытой главы (0-based) или null, если книгу ещё не открывали. */
    val lastChapterIndex: Int?,
    /** Доля прочитанного 0..1 или null, если прогресс неизвестен. */
    val progressFraction: Double?,
)
