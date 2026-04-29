package com.chitalka.epub

internal const val EPUB_OPEN_LOG = "[Chitalka][Epub]"

internal const val BOOK_CACHE_SEGMENT = "book_cache/"

internal const val TIMEOUT_COPY_MS = 180_000L
internal const val TIMEOUT_UNZIP_MS = 600_000L
internal const val TIMEOUT_PREPARE_CHAPTER_MS = 180_000L

class EpubServiceError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

data class EpubSpineItem(
    val index: Int,
    val href: String,
    val idref: String,
    val linear: Boolean,
)

data class EpubTocItem(
    val id: String,
    val href: String,
    val label: String,
    val subitems: List<EpubTocItem>,
)

data class EpubStructure(
    val spine: List<EpubSpineItem>,
    val toc: List<EpubTocItem>,
    /** Корень распакованной книги (`file://.../book_cache/<id>/`). */
    val unpackedRootUri: String,
)

data class FilesystemLibraryMetadata(
    val title: String,
    val author: String,
    val coverFileUri: String?,
)
