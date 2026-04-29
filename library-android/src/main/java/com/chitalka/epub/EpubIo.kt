@file:Suppress("NestedBlockDepth", "ThrowsCount")
package com.chitalka.epub

import android.content.Context
import android.net.Uri
import com.chitalka.debug.ChitalkaMirrorLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream

/** Копирует входной URI в `cacheDir/temp.epub` для последующей распаковки. */
suspend fun copySourceToTempEpub(context: Context, sourceUriString: String): String =
    withContext(Dispatchers.IO) {
        val dest = File(context.cacheDir, "temp.epub")
        val uri = Uri.parse(sourceUriString.trim())
        openSourceStream(context, uri).use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        if (!dest.isFile) {
            throw EpubServiceError(EPUB_ERR_COPY_FAILED)
        }
        ensureFileUri(dest.absolutePath)
    }

private fun openSourceStream(context: Context, uri: Uri): java.io.InputStream {
    return when (uri.scheme?.lowercase()) {
        "file" -> {
            val path = uri.path ?: throw EpubServiceError(EPUB_ERR_BAD_SOURCE_URI)
            File(path).inputStream()
        }
        "content" ->
            context.contentResolver.openInputStream(uri)
                ?: throw EpubServiceError(EPUB_ERR_BAD_SOURCE_URI)
        else -> throw EpubServiceError(EPUB_ERR_BAD_SOURCE_URI)
    }
}

internal fun unzipArchiveToDirectory(zipFile: File, destDir: File) {
    destDir.mkdirs()
    val destCanonical = destDir.canonicalFile
    ZipInputStream(FileInputStream(zipFile)).use { zis ->
        var entry = zis.nextEntry
        while (entry != null) {
            if (entry.name.isEmpty()) {
                zis.closeEntry()
                entry = zis.nextEntry
                continue
            }
            val outFile = File(destDir, entry.name).canonicalFile
            val prefix = destCanonical.path + File.separator
            if (outFile.path != destCanonical.path && !outFile.path.startsWith(prefix)) {
                throw EpubServiceError(EPUB_ERR_UNZIP_FAILED)
            }
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                FileOutputStream(outFile).use { fos -> zis.copyTo(fos) }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }
    }
}

internal fun deleteDirectoryQuiet(dir: File) {
    if (!dir.exists()) return
    dir.walkBottomUp().forEach { it.delete() }
}

internal fun readUtf8FromFileUri(fileUri: String): String {
    val f = File(fileUriToNativePath(ensureFileUri(fileUri)))
    return f.readText(Charsets.UTF_8)
}

internal fun fileExistsAsFile(fileUri: String): Boolean {
    val f = File(fileUriToNativePath(ensureFileUri(fileUri)))
    return f.isFile
}

/** UTF-8 с заменой невалидных последовательностей — чтобы не падать на BOM/битой кодировке в OCF XML. */
private fun File.readXmlTextLenient(): String {
    FileInputStream(this).use { ins ->
        val bytes = ins.readBytes()
        // ISO-8859-1 — последний шанс прочитать заголовочные XML-теги, если декодер всё-таки упал
        // (REPLACE должен был это покрыть, но на отдельных VM/Android-кастомах бывают сюрпризы).
        return try {
            StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .decode(java.nio.ByteBuffer.wrap(bytes))
                .toString()
        } catch (e: Exception) {
            ChitalkaMirrorLog.w(
                "EpubIo",
                "readXmlTextLenient: UTF-8 decode failed file=$absolutePath, fallback to ISO-8859-1: " +
                    "${e.javaClass.simpleName}: ${e.message}",
            )
            bytes.toString(Charsets.ISO_8859_1)
        }
    }
}

internal data class OpfReadResult(
    val opfXml: String,
    val opfDirFileUrl: String,
)

internal fun readOpfFromUnpackedRootFiles(rootUriString: String): OpfReadResult {
    val root = ensureDirectoryRootFileUrl(rootUriString)
    val containerFile = File(fileUriToNativePath("${root}META-INF/container.xml"))
    if (!containerFile.isFile) {
        throw EpubServiceError(EPUB_ERR_CONTAINER_MISSING)
    }
    val containerXml = containerFile.readXmlTextLenient().trimStart('\uFEFF')
    val fp =
        Regex("""\bfull-path\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            .find(containerXml)
            ?.groupValues?.get(1)?.trim()
            ?: throw EpubServiceError(EPUB_ERR_OPF_PARSE_FAILED)
    val opfUri = joinUnderUnpackedRoot(root, fp)
    val opfFile = File(fileUriToNativePath(opfUri))
    if (!opfFile.isFile) {
        throw EpubServiceError(EPUB_ERR_OPF_PARSE_FAILED)
    }
    val opfXml = opfFile.readXmlTextLenient().trimStart('\uFEFF')
    val opfDirUri =
        if (opfUri.contains("/")) {
            opfUri.substring(0, opfUri.lastIndexOf('/') + 1)
        } else {
            root
        }
    return OpfReadResult(opfXml = opfXml, opfDirFileUrl = ensureDirectoryRootFileUrl(opfDirUri))
}
