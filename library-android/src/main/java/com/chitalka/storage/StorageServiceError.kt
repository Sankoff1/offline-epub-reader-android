package com.chitalka.storage

/** Ошибка слоя хранилища. */
class StorageServiceError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
