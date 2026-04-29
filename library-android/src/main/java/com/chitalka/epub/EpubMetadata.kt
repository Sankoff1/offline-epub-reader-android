package com.chitalka.epub

import com.chitalka.debug.ChitalkaMirrorLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Метаданные книги (title/author/cover) из OPF; только файловая система, без WebView. */
suspend fun readFilesystemLibraryMetadata(unpackedRootUri: String): FilesystemLibraryMetadata =
    withContext(Dispatchers.IO) {
        try {
            val (opfXml, opfDirFileUrl) = readOpfFromUnpackedRootFiles(unpackedRootUri)
            val title = pickDcText(opfXml, "title")
            val author = pickDcText(opfXml, "creator")
            val coverRel = extractCoverHrefFromOpf(opfXml)
            if (coverRel == null) {
                return@withContext FilesystemLibraryMetadata(title, author, null)
            }
            val coverAbs = joinUnderUnpackedRoot(opfDirFileUrl, coverRel)
            if (!fileExistsAsFile(coverAbs)) {
                FilesystemLibraryMetadata(title, author, null)
            } else {
                FilesystemLibraryMetadata(title, author, coverAbs)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ChitalkaMirrorLog.w(EPUB_OPEN_LOG, "metadata read failed root=$unpackedRootUri", e)
            FilesystemLibraryMetadata("", "", null)
        }
    }
