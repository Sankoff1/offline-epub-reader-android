package com.chitalka.storage

/**
 * Маппинг кодов из [StorageErrorCodes] на ключи каталога строк.
 * Возвращает `null` для неизвестного кода — UI покажет сам код вместо локализованной строки.
 */
fun storageErrorKey(code: String): String? =
    when (code) {
        STORAGE_ERR_OPEN_FAILED -> "storage.errors.openFailed"
        STORAGE_ERR_GENERIC -> "storage.errors.generic"
        STORAGE_ERR_INVALID_BOOK_ID -> "storage.errors.invalidBookId"
        STORAGE_ERR_INVALID_PROGRESS_OFFSET -> "storage.errors.invalidProgressOffset"
        STORAGE_ERR_INVALID_PROGRESS_SCROLL_RANGE -> "storage.errors.invalidProgressScrollRange"
        else -> null
    }
