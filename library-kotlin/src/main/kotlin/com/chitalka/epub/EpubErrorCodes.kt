package com.chitalka.epub

/**
 * Стабильные коды ошибок открытия EPUB. Android `EpubServiceError` пробрасывает их как [Throwable.message],
 * UI-слой матчит по коду и подменяет на локализованный текст.
 */
const val EPUB_EMPTY_SPINE: String = "EMPTY_SPINE"

const val EPUB_ERR_TIMEOUT_COPY: String = "TIMEOUT_COPY"

const val EPUB_ERR_TIMEOUT_UNZIP: String = "TIMEOUT_UNZIP"

const val EPUB_ERR_TIMEOUT_PREPARE_CHAPTER: String = "TIMEOUT_PREPARE_CHAPTER"

const val EPUB_ERR_COPY_FAILED: String = "COPY_FAILED"

const val EPUB_ERR_UNZIP_FAILED: String = "UNZIP_FAILED"

const val EPUB_ERR_CONTAINER_MISSING: String = "CONTAINER_MISSING"

const val EPUB_ERR_OPF_PARSE_FAILED: String = "OPF_PARSE_FAILED"

const val EPUB_ERR_CHAPTER_READ_FAILED: String = "CHAPTER_READ_FAILED"

const val EPUB_ERR_BAD_SOURCE_URI: String = "BAD_SOURCE_URI"

/** Внутренняя ошибка ридера (нарушен инвариант сервиса). Для пользователя — «попробуйте переоткрыть книгу». */
const val EPUB_ERR_INTERNAL: String = "INTERNAL"
