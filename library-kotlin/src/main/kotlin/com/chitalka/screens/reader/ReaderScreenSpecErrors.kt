package com.chitalka.screens.reader

import com.chitalka.epub.EPUB_EMPTY_SPINE
import com.chitalka.epub.EPUB_ERR_BAD_SOURCE_URI
import com.chitalka.epub.EPUB_ERR_CHAPTER_READ_FAILED
import com.chitalka.epub.EPUB_ERR_CONTAINER_MISSING
import com.chitalka.epub.EPUB_ERR_COPY_FAILED
import com.chitalka.epub.EPUB_ERR_INTERNAL
import com.chitalka.epub.EPUB_ERR_OPF_PARSE_FAILED
import com.chitalka.epub.EPUB_ERR_TIMEOUT_COPY
import com.chitalka.epub.EPUB_ERR_TIMEOUT_PREPARE_CHAPTER
import com.chitalka.epub.EPUB_ERR_TIMEOUT_UNZIP
import com.chitalka.epub.EPUB_ERR_UNZIP_FAILED

/**
 * Результат маппинга `ReaderOpenErrorKind` на показ в UI: либо ключ в каталоге строк
 * (UI сам резолвит через адаптер), либо литеральная строка (когда EPUB вернул свой текст).
 */
sealed class ReaderOpenErrorText {
    data class Key(val i18nKey: String) : ReaderOpenErrorText()

    data class Literal(val text: String) : ReaderOpenErrorText()
}

fun ReaderScreenSpec.readerOpenErrorText(
    kind: ReaderScreenSpec.ReaderOpenErrorKind,
): ReaderOpenErrorText =
    when (kind) {
        is ReaderScreenSpec.ReaderOpenErrorKind.Epub -> epubOpenErrorText(kind.message)
        is ReaderScreenSpec.ReaderOpenErrorKind.Other ->
            kind.message?.trim()?.takeIf { it.isNotEmpty() }
                ?.let { ReaderOpenErrorText.Literal(it) }
                ?: ReaderOpenErrorText.Key(ReaderScreenSpec.I18nKeys.ERR_UNKNOWN)
        ReaderScreenSpec.ReaderOpenErrorKind.Unknown ->
            ReaderOpenErrorText.Key(ReaderScreenSpec.I18nKeys.ERR_UNKNOWN)
    }

private fun epubOpenErrorText(rawMessage: String): ReaderOpenErrorText {
    val m = rawMessage.trim()
    val key =
        when (m) {
            EPUB_EMPTY_SPINE -> ReaderScreenSpec.I18nKeys.ERR_EMPTY_SPINE
            EPUB_ERR_TIMEOUT_COPY -> ReaderScreenSpec.I18nKeys.ERR_TIMEOUT_COPY
            EPUB_ERR_TIMEOUT_UNZIP -> ReaderScreenSpec.I18nKeys.ERR_TIMEOUT_UNZIP
            EPUB_ERR_TIMEOUT_PREPARE_CHAPTER -> ReaderScreenSpec.I18nKeys.ERR_TIMEOUT_PREPARE_CHAPTER
            EPUB_ERR_COPY_FAILED -> ReaderScreenSpec.I18nKeys.ERR_COPY_FAILED
            EPUB_ERR_UNZIP_FAILED -> ReaderScreenSpec.I18nKeys.ERR_UNZIP_FAILED
            EPUB_ERR_CONTAINER_MISSING -> ReaderScreenSpec.I18nKeys.ERR_CONTAINER_MISSING
            EPUB_ERR_OPF_PARSE_FAILED -> ReaderScreenSpec.I18nKeys.ERR_OPF_PARSE_FAILED
            EPUB_ERR_CHAPTER_READ_FAILED -> ReaderScreenSpec.I18nKeys.ERR_CHAPTER_READ_FAILED
            EPUB_ERR_BAD_SOURCE_URI -> ReaderScreenSpec.I18nKeys.ERR_BAD_SOURCE_URI
            EPUB_ERR_INTERNAL -> ReaderScreenSpec.I18nKeys.ERR_INTERNAL
            else -> null
        }
    key?.let { return ReaderOpenErrorText.Key(it) }
    return if (m.isNotEmpty()) {
        ReaderOpenErrorText.Literal(m)
    } else {
        ReaderOpenErrorText.Key(ReaderScreenSpec.I18nKeys.ERR_OPEN_FAILED)
    }
}
