package com.chitalka.core.types

/** Запись о книге в локальной библиотеке (SQLite). */
data class LibraryBookRecord(
    val bookId: String,
    val fileUri: String,
    val title: String,
    val author: String,
    val fileSizeBytes: Long,
    val coverUri: String?,
    val addedAt: Long,
    /** Число глав (длина spine). 0 пока книга ни разу не открывалась в читалке. */
    val totalChapters: Int,
    /** Отмечена ли книга как избранная. */
    val isFavorite: Boolean,
    /** Время перемещения в корзину (мс epoch) или `null`, если книга активна. */
    val deletedAt: Long?,
)
