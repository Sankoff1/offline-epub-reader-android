package com.chitalka.picker

/** Результат выбора EPUB через системный picker. */
sealed class EpubPickResult {
    data class Ok(
        val uri: String,
        val bookId: String,
    ) : EpubPickResult()

    data object Canceled : EpubPickResult()

    data class Error(
        val messageKey: String,
    ) : EpubPickResult()
}
