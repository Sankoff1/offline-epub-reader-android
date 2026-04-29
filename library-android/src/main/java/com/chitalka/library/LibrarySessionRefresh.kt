package com.chitalka.library

import com.chitalka.debug.ChitalkaMirrorLog
import com.chitalka.storage.StorageService

/** Обновляет [LibrarySessionState.bookCount] из SQLite. На ошибке — счётчик становится 0. */
suspend fun LibrarySessionState.refreshBookCount(storage: StorageService) {
    try {
        updateBookCount(storage.countLibraryBooks())
    } catch (e: Exception) {
        ChitalkaMirrorLog.w("LibrarySession", "refreshBookCount failed", e)
        updateBookCount(0)
    }
}
