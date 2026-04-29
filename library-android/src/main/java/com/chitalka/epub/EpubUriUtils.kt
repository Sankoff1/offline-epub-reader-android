@file:Suppress("ReturnCount")
package com.chitalka.epub

import com.chitalka.debug.ChitalkaMirrorLog
import java.net.URI
import java.net.URLDecoder

fun ensureFileUri(pathOrUri: String): String {
    var t = pathOrUri.trim()
    if (t.startsWith("null/")) {
        t = t.removePrefix("null/")
    }
    if (t.startsWith("content://")) {
        return t
    }
    // [file:///path], [file://host/path] — уже нормализованы.
    if (t.startsWith("file://")) {
        return t
    }
    // [URI.resolve] на Windows часто даёт [file:/C:/...] (один слэш после file:). Если скормить это
    // старым правилам ниже, получится [file:///file:/C:/...] и путь к OPF ломается.
    if (t.startsWith("file:/")) {
        val after = t.removePrefix("file:").replace('\\', '/')
        return when {
            after.matches(Regex("^/[a-zA-Z]:/.*")) ->
                "file://$after"
            after.startsWith("/") ->
                "file://$after"
            else ->
                "file:///$after"
        }
    }
    val normalized = t.replace('\\', '/')
    if (normalized.startsWith("/")) {
        return "file://$normalized"
    }
    if (Regex("^[a-zA-Z]:").containsMatchIn(normalized.take(2))) {
        val rest = normalized.trimStart('/')
        return "file:///$rest"
    }
    return "file:///$normalized"
}

internal fun ensureDirectoryRootFileUrl(dirUri: String): String {
    val u = ensureFileUri(dirUri.trim())
    return if (u.endsWith("/")) u else "$u/"
}

/** Путь для нативного ZipInputStream / java.io.File. */
fun fileUriToNativePath(uri: String): String {
    var path = uri.trim()
    if (path.startsWith("file://")) {
        path = path.removePrefix("file://")
        if (path.matches(Regex("^/[a-zA-Z]:.*"))) {
            path = path.removePrefix("/")
        }
    }
    return URLDecoder.decode(path, Charsets.UTF_8.name())
}

internal fun stripXmlFragment(s: String): String = (s.split('#').firstOrNull() ?: "").trim()

internal fun joinUnderUnpackedRoot(rootDirFileUrl: String, relativePath: String): String {
    val r = stripXmlFragment(relativePath).replace('\\', '/').trimStart('/')
    val base = if (rootDirFileUrl.endsWith("/")) rootDirFileUrl else "$rootDirFileUrl/"
    // [URI.resolve] падает на путях с непечатаемыми символами; для распакованного EPUB достаточно
    // конкатенации — `r` уже без `..`, потому что fragment вырезан и slashes нормализованы.
    return try {
        val resolved = URI.create(base).resolve(r)
        ensureFileUri(resolved.toString())
    } catch (e: Exception) {
        ChitalkaMirrorLog.w(
            "EpubUriUtils",
            "joinUnderUnpackedRoot: URI.resolve failed base=$base rel=$r, " +
                "${e.javaClass.simpleName}: ${e.message}",
        )
        ensureFileUri("$base$r")
    }
}
