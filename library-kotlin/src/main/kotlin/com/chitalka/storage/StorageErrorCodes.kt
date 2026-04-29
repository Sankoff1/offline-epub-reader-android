package com.chitalka.storage

/**
 * Стабильные коды ошибок слоя хранилища. Android `StorageServiceError` пробрасывает их через
 * `Throwable.message`; UI-слой сам решает, мапить на локализованную строку или просто логировать.
 *
 * Эти строки — часть контракта, не локализованный текст. Не переименовывать без миграции UI.
 */
const val STORAGE_ERR_OPEN_FAILED: String = "STORAGE_OPEN_FAILED"

const val STORAGE_ERR_GENERIC: String = "STORAGE_GENERIC"

const val STORAGE_ERR_INVALID_BOOK_ID: String = "STORAGE_INVALID_BOOK_ID"

const val STORAGE_ERR_INVALID_PROGRESS_OFFSET: String = "STORAGE_INVALID_PROGRESS_OFFSET"

const val STORAGE_ERR_INVALID_PROGRESS_SCROLL_RANGE: String = "STORAGE_INVALID_PROGRESS_SCROLL_RANGE"
