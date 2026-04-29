@file:Suppress("TooManyFunctions", "ReturnCount", "MagicNumber")
package com.chitalka.epub

import com.chitalka.debug.ChitalkaMirrorLog
import java.io.File
import java.net.URI
import java.net.URLDecoder

internal fun escapeHtmlAttrValue(raw: String, quote: String): String {
    var s = raw.replace("&", "&amp;").replace("<", "&lt;")
    s =
        if (quote == "\"") {
            s.replace("\"", "&quot;")
        } else {
            s.replace("'", "&#39;")
        }
    return s
}

internal fun decodeBasicXmlEntities(s: String): String {
    return s.replace(Regex("&#x([0-9a-fA-F]+);")) { m ->
        // Слишком длинные значения (overflow Int) оставляем как было — это не валидная сущность.
        m.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: m.value
    }.replace(Regex("&#(\\d+);")) { m ->
        m.groupValues[1].toIntOrNull(10)?.toChar()?.toString() ?: m.value
    }.replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&apos;", "'")
        .replace("&quot;", "\"")
        .replace("&amp;", "&")
        .trim()
}

internal fun injectReaderViewportAndReflowCss(html: String): String {
    val withoutOldViewport =
        html.replace(
            Regex(
                """<meta\b[^>]*\bname\s*=\s*["']viewport["'][^>]*>""",
                RegexOption.IGNORE_CASE,
            ),
            "",
        )
    val block =
        """
<meta name="viewport" content="width=device-width, initial-scale=1">
<style type="text/css" id="chitalka-reader-reflow">
html{-webkit-text-size-adjust:100%;text-size-adjust:100%;}
body{margin:0!important;padding:16px 20px env(safe-area-inset-bottom, 16px)!important;box-sizing:border-box;width:100%!important;max-width:100vw!important;overflow-x:hidden!important;word-wrap:break-word;overflow-wrap:break-word;line-height:1.55;}
p{margin:0 0 1em 0;}
img,svg,video,object,embed,iframe{max-width:100%!important;height:auto!important;}
table{max-width:100%!important;}
pre,code{white-space:pre-wrap;word-wrap:break-word;max-width:100%;}
</style>
        """.trimIndent() + "\n"

    Regex("</head>", RegexOption.IGNORE_CASE).find(withoutOldViewport)?.let { headClose ->
        val i = headClose.range.first
        return withoutOldViewport.substring(0, i) + block + withoutOldViewport.substring(i)
    }
    Regex("<head\\b[^>]*>", RegexOption.IGNORE_CASE).find(withoutOldViewport)?.let { headOpen ->
        val pos = headOpen.range.last + 1
        return withoutOldViewport.substring(0, pos) + block + withoutOldViewport.substring(pos)
    }
    Regex("<html\\b[^>]*>", RegexOption.IGNORE_CASE).find(withoutOldViewport)?.let { htmlOpen ->
        val pos = htmlOpen.range.last + 1
        return withoutOldViewport.substring(0, pos) + "<head>$block</head>" + withoutOldViewport.substring(pos)
    }
    return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/>$block</head><body>$withoutOldViewport</body></html>"
}

internal fun pickDcText(opfXml: String, localName: String): String {
    val re =
        Regex(
            """<(?:dc:$localName|[a-z]+:$localName)\b[^>]*>([\s\S]*?)</(?:dc:$localName|[a-z]+:$localName)>""",
            RegexOption.IGNORE_CASE,
        )
    val m = re.find(opfXml) ?: return ""
    val inner = m.groupValues.getOrNull(1) ?: return ""
    return decodeBasicXmlEntities(
        inner.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim(),
    )
}

private fun escapeRegExp(s: String): String = Regex.escape(s)

internal fun extractItemHrefById(opfXml: String, itemId: String): String? {
    val idNeedle = Regex("\\bid\\s*=\\s*[\"']${escapeRegExp(itemId)}[\"']", RegexOption.IGNORE_CASE)
    val itemTagRe = Regex("""<item\b[^>]*/?>""", RegexOption.IGNORE_CASE)
    itemTagRe.findAll(opfXml).forEach { im ->
        val tag = im.value
        if (!idNeedle.containsMatchIn(tag)) return@forEach
        val hm = Regex("""\bhref\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(tag)
        val href = hm?.groupValues?.getOrNull(1)?.trim()
        if (!href.isNullOrEmpty()) {
            return stripXmlFragment(href)
        }
    }
    return null
}

internal fun extractCoverHrefFromOpf(opfXml: String): String? {
    val metaTagRe = Regex("""<meta\b([^>]*)/?>""", RegexOption.IGNORE_CASE)
    metaTagRe.findAll(opfXml).forEach { mm ->
        val attrs = mm.groupValues[1]
        if (!Regex("""name\s*=\s*["']cover["']""", RegexOption.IGNORE_CASE).containsMatchIn(attrs)) {
            return@forEach
        }
        val cm = Regex("""\bcontent\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(attrs)
        val coverItemId = cm?.groupValues?.getOrNull(1)?.trim() ?: return@forEach
        extractItemHrefById(opfXml, coverItemId)?.let { return it }
    }
    val covItem =
        Regex(
            """<item\b([^>]*properties\s*=\s*["'][^"']*cover-image[^"']*["'][^>]*)/?>""",
            RegexOption.IGNORE_CASE,
        ).find(opfXml)
    if (covItem != null) {
        val attrs = covItem.groupValues[1]
        val hm = Regex("""\bhref\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(attrs)
        val href = hm?.groupValues?.getOrNull(1)?.trim()
        if (!href.isNullOrEmpty()) {
            return stripXmlFragment(href)
        }
    }
    val ref = Regex("""<reference\b[^>]*type\s*=\s*["']cover["'][^>]*/?>""", RegexOption.IGNORE_CASE).find(opfXml)
    if (ref != null) {
        val hm = Regex("""\bhref\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(ref.value)
        val href = hm?.groupValues?.getOrNull(1)?.trim()
        if (!href.isNullOrEmpty()) {
            return stripXmlFragment(href)
        }
    }
    return null
}

internal fun extractManifestIdToHrefMap(opfXml: String): Map<String, String> {
    val map = linkedMapOf<String, String>()
    val itemRe = Regex("""<(?:[\w]*:)?item\b[^>]*>""", RegexOption.IGNORE_CASE)
    itemRe.findAll(opfXml).forEach { m ->
        val tag = m.value
        val idM = Regex("""\bid\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(tag)
        val hrefM = Regex("""\bhref\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(tag)
        val id = idM?.groupValues?.getOrNull(1)?.trim()
        val href = hrefM?.groupValues?.getOrNull(1)?.trim()
        if (!id.isNullOrEmpty() && !href.isNullOrEmpty()) {
            map[id] = stripXmlFragment(href)
        }
    }
    return map
}

internal fun extractSpineItemrefsFromOpf(opfXml: String): List<Pair<String, Boolean>> {
    val spineBlock =
        Regex("""<(?:[\w]*:)?spine\b[^>]*>([\s\S]*?)</(?:[\w]*:)?spine>""", RegexOption.IGNORE_CASE)
            .find(opfXml)
            ?: return emptyList()
    val body = spineBlock.groupValues[1]
    val out = mutableListOf<Pair<String, Boolean>>()
    val itemrefRe = Regex("""<(?:[\w]*:)?itemref\b([^>]*)/?>""", RegexOption.IGNORE_CASE)
    itemrefRe.findAll(body).forEach { im ->
        val attrs = im.groupValues[1]
        val idrefM = Regex("""\bidref\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(attrs)
        val idref = idrefM?.groupValues?.getOrNull(1)?.trim() ?: return@forEach
        val linearM = Regex("""\blinear\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(attrs)
        val linear = linearM?.groupValues?.getOrNull(1)?.let { it.lowercase() != "no" } ?: true
        out.add(idref to linear)
    }
    return out
}

internal fun buildSpineFromOpfXml(opfXml: String): List<EpubSpineItem> {
    val manifest = extractManifestIdToHrefMap(opfXml)
    val refs = extractSpineItemrefsFromOpf(opfXml)
    val spine = mutableListOf<EpubSpineItem>()
    for ((idref, linear) in refs) {
        val href = manifest[idref]
        if (href == null) {
            ChitalkaMirrorLog.w(EPUB_OPEN_LOG, "itemref без href в manifest $idref")
            continue
        }
        spine.add(EpubSpineItem(index = spine.size, href = href, idref = idref, linear = linear))
    }
    return spine
}

internal fun resolveChapterAssetUri(unpackedRootUri: String, chapterFileUri: String, src: String): String {
    val pathPart = src.trim().split('#').firstOrNull()?.trim().orEmpty()
    if (pathPart.isEmpty()) return ""
    // best-effort: невалидное %-кодирование — оставляем сырую строку, дальше File её всё равно проверит.
    val decoded = try {
        URLDecoder.decode(pathPart, Charsets.UTF_8.name())
    } catch (e: Exception) {
        ChitalkaMirrorLog.w(
            "EpubOpfXml",
            "resolveChapterAssetUri: URLDecoder.decode failed src=$src, " +
                "${e.javaClass.simpleName}: ${e.message}",
        )
        pathPart
    }
    val cleanPath = decoded.replace('\\', '/')
    // best-effort: любой сбой при разборе пути → пустая строка, вызывающий покажет битый src.
    return try {
        val baseDir =
            if (cleanPath.startsWith("/")) {
                val rootUri = if (unpackedRootUri.endsWith("/")) unpackedRootUri else "$unpackedRootUri/"
                File(fileUriToNativePath(rootUri))
            } else {
                File(fileUriToNativePath(chapterFileUri)).parentFile ?: return ""
            }
        val relPath = cleanPath.removePrefix("/")
        val target = File(baseDir, relPath).canonicalFile
        if (target.isFile) {
            ensureFileUri(target.toURI().toASCIIString())
        } else {
            val ci = resolveFileCaseInsensitive(baseDir, relPath)
            if (ci != null && ci.isFile) {
                ensureFileUri(ci.toURI().toASCIIString())
            } else {
                ensureFileUri(target.toURI().toASCIIString())
            }
        }
    } catch (e: Exception) {
        ChitalkaMirrorLog.w(
            "EpubOpfXml",
            "resolveChapterAssetUri: path resolve failed root=$unpackedRootUri chapter=$chapterFileUri src=$src, " +
                "${e.javaClass.simpleName}: ${e.message}",
        )
        ""
    }
}

private fun resolveFileCaseInsensitive(baseDir: File, relPath: String): File? {
    var current: File = baseDir
    val segments = relPath.split('/').filter { it.isNotEmpty() && it != "." }
    for (seg in segments) {
        if (seg == "..") {
            current = current.parentFile ?: return null
            continue
        }
        val kids = current.listFiles() ?: return null
        val match = kids.firstOrNull { it.name.equals(seg, ignoreCase = true) } ?: return null
        current = match
    }
    return current
}
