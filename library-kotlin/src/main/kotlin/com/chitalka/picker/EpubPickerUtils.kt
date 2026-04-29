@file:Suppress("ReturnCount")
package com.chitalka.picker

private val EPUB_EXTENSION_PATTERN = Regex("""\.epub$""", RegexOption.IGNORE_CASE)
private val PATH_PREFIX_PATTERN = Regex("""^.*[/\\]""")

fun deriveBookId(fileName: String): String {
    val base = fileName.replace(PATH_PREFIX_PATTERN, "").trim()
    val withoutExt = base.replace(EPUB_EXTENSION_PATTERN, "").trim()
    return if (withoutExt.isNotEmpty()) {
        withoutExt
    } else {
        "book_${System.currentTimeMillis()}"
    }
}

fun isEpubFileName(name: String): Boolean = name.trim().lowercase().endsWith(".epub")

fun isLikelyEpubAsset(
    name: String,
    mimeType: String?,
    uriString: String,
): Boolean {
    if (isEpubFileName(name)) {
        return true
    }
    val mime = mimeType?.trim()?.lowercase().orEmpty()
    if (mime.contains("epub")) {
        return true
    }
    val pathOnly = uriString.substringBefore('?')
    return EPUB_EXTENSION_PATTERN.containsMatchIn(pathOnly)
}

/** MIME-типы EPUB для системного OpenDocument; `application/octet-stream` и wildcard — fallback. */
fun epubOpenDocumentMimeTypes(): Array<String> =
    arrayOf(
        "application/epub+zip",
        "application/octet-stream",
        "application/x-fictionbook+xml",
        "*/*",
    )
