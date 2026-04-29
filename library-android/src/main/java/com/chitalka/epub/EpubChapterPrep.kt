package com.chitalka.epub

import com.chitalka.debug.ChitalkaMirrorLog

private val IMG_TAG_REGEX = Regex("""<img\b[^>]*>""", RegexOption.IGNORE_CASE)
private val SRC_ATTR_REGEX = Regex("""\bsrc\s*=\s*(["'])([\s\S]*?)\1""", RegexOption.IGNORE_CASE)
private val HTTP_URL_REGEX = Regex("^https?://", RegexOption.IGNORE_CASE)

// Запас на каждый <img>: типичный путь file:// после переписывания src.
private const val IMG_REWRITE_HEADROOM = 32

internal fun prepareChapterBodyForReader(unpackedRootUri: String, htmlPath: String): String {
    val chapterUri = ensureFileUri(htmlPath)
    val html =
        try {
            readUtf8FromFileUri(chapterUri)
        } catch (err: Exception) {
            ChitalkaMirrorLog.w(EPUB_OPEN_LOG, "chapter read failed $chapterUri", err)
            throw EpubServiceError(EPUB_ERR_CHAPTER_READ_FAILED, err)
        }

    val matches = IMG_TAG_REGEX.findAll(html).toList()
    if (matches.isEmpty()) {
        ChitalkaMirrorLog.d(
            EPUB_OPEN_LOG,
            "prepareChapter: chapter=$chapterUri imgTagsFound=0 rewritten=0",
        )
        return injectReaderViewportAndReflowCss(html)
    }

    val out = StringBuilder(html.length + matches.size * IMG_REWRITE_HEADROOM)
    var cursor = 0
    var rewritten = 0
    for (match in matches) {
        val fullTag = match.value
        val srcMatch = SRC_ATTR_REGEX.find(fullTag)
        val newTag =
            if (srcMatch == null) {
                fullTag
            } else {
                rewriteLocalImageSrcInTag(
                    fullTag = fullTag,
                    srcAttrFull = srcMatch.value,
                    quote = srcMatch.groupValues[1],
                    srcVal = srcMatch.groupValues[2].trim(),
                    chapterUri = chapterUri,
                    unpackedRootUri = unpackedRootUri,
                )
            }
        if (newTag != fullTag) rewritten++
        out.append(html, cursor, match.range.first)
        out.append(newTag)
        cursor = match.range.last + 1
    }
    out.append(html, cursor, html.length)

    ChitalkaMirrorLog.d(
        EPUB_OPEN_LOG,
        "prepareChapter: chapter=$chapterUri imgTagsFound=${matches.size} rewritten=$rewritten",
    )
    return injectReaderViewportAndReflowCss(out.toString())
}

@Suppress("LongParameterList")
private fun rewriteLocalImageSrcInTag(
    fullTag: String,
    srcAttrFull: String,
    quote: String,
    srcVal: String,
    chapterUri: String,
    unpackedRootUri: String,
): String {
    if (srcVal.isEmpty() || srcVal.startsWith("data:")) return fullTag
    if (HTTP_URL_REGEX.containsMatchIn(srcVal)) return fullTag

    val assetUri = resolveChapterAssetUri(unpackedRootUri, chapterUri, srcVal)
    if (!assetUri.startsWith("file://")) {
        ChitalkaMirrorLog.w(EPUB_OPEN_LOG, "img src не удалось разрешить: src=$srcVal chapter=$chapterUri")
        return fullTag
    }
    if (!fileExistsAsFile(assetUri)) {
        ChitalkaMirrorLog.w(EPUB_OPEN_LOG, "img файл не найден: src=$srcVal resolved=$assetUri")
        return fullTag
    }
    val escaped = escapeHtmlAttrValue(assetUri, quote)
    return fullTag.replace(srcAttrFull, "src=$quote$escaped$quote")
}
