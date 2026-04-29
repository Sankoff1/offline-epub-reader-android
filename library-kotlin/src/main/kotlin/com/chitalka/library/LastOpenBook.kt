@file:Suppress("MatchingDeclarationName")
package com.chitalka.library

import com.chitalka.debug.DebugLogLevel
import com.chitalka.debug.debugLogAppend

/**
 * Абстракция над key-value хранилищем. Реализация на Android — поверх SharedPreferences.
 */
interface LastOpenBookPersistence {
    suspend fun getItem(key: String): String?
    suspend fun setItem(key: String, value: String)
    suspend fun removeItem(key: String)
}

/**
 * Ключ последней открытой книги: выставляется при открытии читалки
 * и очищается при возврате в библиотеку.
 */
const val LAST_OPEN_BOOK_STORAGE_KEY = "chitalka_last_open_book_id"

/** Возвращает исходную строку, если она не пуста после trim; иначе `null`. Trim применяется только для проверки. */
private fun String?.storedBookIdOrNull(): String? {
    val raw = this ?: return null
    return if (raw.trim().isEmpty()) null else raw
}

suspend fun getLastOpenBookId(storage: LastOpenBookPersistence): String? =
    try {
        storage.getItem(LAST_OPEN_BOOK_STORAGE_KEY).storedBookIdOrNull()
    } catch (e: Exception) {
        // best-effort: при сбое чтения покажем библиотеку, а не упавший сплеш —
        // но сам факт сбоя в логах нужен, чтобы заметить регрессию хранилища.
        debugLogAppend(
            DebugLogLevel.Warn,
            "getLastOpenBookId: read failed for key=$LAST_OPEN_BOOK_STORAGE_KEY " +
                "(${e.javaClass.simpleName}: ${e.message})",
        )
        null
    }

suspend fun setLastOpenBookId(storage: LastOpenBookPersistence, bookId: String) {
    if (bookId.isBlank()) return
    try {
        storage.setItem(LAST_OPEN_BOOK_STORAGE_KEY, bookId)
    } catch (e: Exception) {
        // потеря ключа не ломает текущее чтение, только автооткрытие в следующий запуск.
        debugLogAppend(
            DebugLogLevel.Warn,
            "setLastOpenBookId: write failed for key=$LAST_OPEN_BOOK_STORAGE_KEY bookId=$bookId " +
                "(${e.javaClass.simpleName}: ${e.message})",
        )
    }
}

suspend fun clearLastOpenBookId(storage: LastOpenBookPersistence) {
    try {
        storage.removeItem(LAST_OPEN_BOOK_STORAGE_KEY)
    } catch (e: Exception) {
        debugLogAppend(
            DebugLogLevel.Warn,
            "clearLastOpenBookId: remove failed for key=$LAST_OPEN_BOOK_STORAGE_KEY " +
                "(${e.javaClass.simpleName}: ${e.message})",
        )
    }
}
