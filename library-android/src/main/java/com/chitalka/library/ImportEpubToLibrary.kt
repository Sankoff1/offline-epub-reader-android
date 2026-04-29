@file:Suppress(
    "TooGenericExceptionCaught",
    "ReturnCount",
    "MagicNumber",
    "LongParameterList",
    "LongMethod",
    "CyclomaticComplexMethod",
    "NestedBlockDepth",
    "ThrowsCount",
)

package com.chitalka.library

import android.content.Context
import com.chitalka.core.types.LibraryBookRecord
import com.chitalka.debug.ChitalkaMirrorLog
import com.chitalka.epub.EpubService
import com.chitalka.epub.EpubServiceError
import com.chitalka.epub.copySourceToTempEpub
import com.chitalka.epub.ensureFileUri
import com.chitalka.epub.fileUriToNativePath
import com.chitalka.epub.readFilesystemLibraryMetadata
import com.chitalka.storage.StorageService
import com.chitalka.storage.restoreBookFromTrash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

private const val LOG_TAG = "[Chitalka][Импорт]"
private const val EPUB_SUBDIR = "library_epubs"
private const val COVERS_SUBDIR = "library_covers"

data class ImportEpubOptions(
    /** Не вызывать колбэки успеха / дубликата (используется при тихом импорте). */
    val suppressSuccessFeedback: Boolean = false,
    val onDuplicateInLibrary: (() -> Unit)? = null,
    val onImportedToLibrary: (() -> Unit)? = null,
)

/** Подписи-фолбэки для метаданных книги (заголовок / автор), когда EPUB их не содержит. */
data class BookFallbackLabels(
    val untitled: String,
    val unknownAuthor: String,
)

data class ImportEpubOutcome(
    val stableUri: String,
    val bookId: String,
    val wasAlreadyInLibrary: Boolean,
)

private fun logImportStage(message: String, extra: Map<String, Any?>? = null) {
    if (extra != null) {
        ChitalkaMirrorLog.d(LOG_TAG, "$message $extra")
    } else {
        ChitalkaMirrorLog.d(LOG_TAG, message)
    }
}

private fun sanitizeFileStem(bookId: String): String {
    return bookId.trim()
        .replace(Regex("""[/\\?%*:|"<>.\u0000]"""), "_")
        .replace(Regex("\\s+"), " ")
        .take(120)
        .trim()
        .replace(Regex("^_+|_+$"), "")
        .ifEmpty { "book" }
}

private fun shortFileSuffix(bookId: String): String {
    var h = 0
    for (element in bookId) {
        h = 31 * h + element.code
    }
    return h.toUInt().toString(36)
}

private fun coverExtensionFromUri(uri: String): String {
    val lower = uri.substringBefore('?').lowercase()
    return when {
        lower.endsWith(".png") -> ".png"
        lower.endsWith(".webp") -> ".webp"
        lower.endsWith(".gif") -> ".gif"
        lower.endsWith(".jpeg") -> ".jpeg"
        lower.endsWith(".jpg") -> ".jpg"
        else -> ".jpg"
    }
}

/**
 * Копирует EPUB в постоянный каталог приложения, извлекает метаданные и обложку через [EpubService],
 * сохраняет запись в [StorageService]. Уведомления о результате — через колбэки в [ImportEpubOptions].
 */
suspend fun importEpubToLibrary(
    context: Context,
    sourceUri: String,
    bookId: String,
    storage: StorageService,
    fallbackLabels: BookFallbackLabels,
    options: ImportEpubOptions = ImportEpubOptions(),
): ImportEpubOutcome {
    try {
        val baseFile = context.filesDir

        val existing = storage.getLibraryBook(bookId)
        if (existing != null) {
            val existingPath = File(fileUriToNativePath(ensureFileUri(existing.fileUri)))
            if (existingPath.isFile) {
                if (existing.deletedAt != null) {
                    storage.restoreBookFromTrash(bookId)
                    logImportStage("Книга восстановлена из корзины", mapOf("bookId" to bookId))
                    if (!options.suppressSuccessFeedback) {
                        options.onImportedToLibrary?.invoke()
                    }
                    return ImportEpubOutcome(
                        stableUri = existing.fileUri,
                        bookId = bookId,
                        wasAlreadyInLibrary = false,
                    )
                }
                logImportStage("Книга уже в библиотеке — повторный импорт пропущен", mapOf("bookId" to bookId))
                if (!options.suppressSuccessFeedback) {
                    options.onDuplicateInLibrary?.invoke()
                }
                return ImportEpubOutcome(
                    stableUri = existing.fileUri,
                    bookId = bookId,
                    wasAlreadyInLibrary = true,
                )
            }
            logImportStage("Запись книги есть, но файл пропал — импортируем заново", mapOf("bookId" to bookId))
        }

        val stem = sanitizeFileStem(bookId)
        val fileBase = "${stem}__${shortFileSuffix(bookId)}"
        val epubDir = File(baseFile, EPUB_SUBDIR)
        val coversDir = File(baseFile, COVERS_SUBDIR)
        epubDir.mkdirs()
        coversDir.mkdirs()

        val stableFile = File(epubDir, "$fileBase.epub")
        val stableUri = ensureFileUri(stableFile.absolutePath)

        val tempEpubUri = copySourceToTempEpub(context, sourceUri)
        withContext(Dispatchers.IO) {
            File(fileUriToNativePath(ensureFileUri(tempEpubUri))).inputStream().use { input ->
                FileOutputStream(stableFile).use { output -> input.copyTo(output) }
            }
            runCatching { File(fileUriToNativePath(ensureFileUri(tempEpubUri))).delete() }
        }

        logImportStage("Копирование завершено", mapOf("bookId" to bookId))

        if (!stableFile.isFile) {
            throw EpubServiceError("Не удалось сохранить файл EPUB в библиотеку.")
        }
        val fileSizeBytes = stableFile.length().coerceAtLeast(0)

        val svc = EpubService(context, stableUri)
        try {
            svc.unpackThroughStep5()
            val unpacked = svc.getUnpackedRootUri()
                ?: throw EpubServiceError("Распаковка не создала каталог книги.")
            val fsMeta = readFilesystemLibraryMetadata(unpacked)
            val labels = fallbackLabels

            var coverUri: String? = null
            var coverSrc = fsMeta.coverFileUri
            if (coverSrc == null) {
                runCatching {
                    svc.open()
                    svc.resolveFallbackCoverFromFirstSpineImage()
                }.onSuccess { fallback ->
                    if (fallback != null) {
                        coverSrc = fallback
                        logImportStage("Обложка взята из первой страницы", mapOf("bookId" to bookId))
                    }
                }.onFailure { e ->
                    ChitalkaMirrorLog.w(LOG_TAG, "fallback cover: open()/resolve упал bookId=$bookId", e)
                }
            }
            coverSrc?.let { src ->
                val coverFile = File(fileUriToNativePath(ensureFileUri(src)))
                if (coverFile.isFile) {
                    val ext = coverExtensionFromUri(src)
                    val destCover = File(coversDir, "${fileBase}_cover$ext")
                    runCatching {
                        FileInputStream(coverFile).use { input ->
                            FileOutputStream(destCover).use { output -> input.copyTo(output) }
                        }
                    }.onSuccess {
                        coverUri = ensureFileUri(destCover.absolutePath)
                    }.onFailure { e ->
                        ChitalkaMirrorLog.w(LOG_TAG, "не удалось скопировать обложку bookId=$bookId", e)
                    }
                }
            }

            val row =
                LibraryBookRecord(
                    bookId = bookId,
                    fileUri = stableUri,
                    title = fsMeta.title.trim().ifEmpty { labels.untitled },
                    author = fsMeta.author.trim().ifEmpty { labels.unknownAuthor },
                    fileSizeBytes = fileSizeBytes,
                    coverUri = coverUri,
                    addedAt = System.currentTimeMillis(),
                    totalChapters = 0,
                    isFavorite = false,
                    deletedAt = null,
                )
            storage.addBook(row)
            logImportStage("Книга добавлена в базу", mapOf("bookId" to bookId, "title" to row.title))
            if (!options.suppressSuccessFeedback) {
                options.onImportedToLibrary?.invoke()
            }
            return ImportEpubOutcome(stableUri = stableUri, bookId = bookId, wasAlreadyInLibrary = false)
        } finally {
            svc.destroy()
        }
    } catch (e: Exception) {
        val message = e.message ?: e.toString()
        logImportStage("Ошибка импорта", mapOf("bookId" to bookId, "message" to message))
        throw e
    }
}
