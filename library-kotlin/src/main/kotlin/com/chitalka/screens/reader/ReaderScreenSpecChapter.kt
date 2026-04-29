package com.chitalka.screens.reader

import com.chitalka.ui.readerview.ReaderPageDirection
import kotlin.math.floor
import kotlin.math.max

/** Резерв ширины анимации перелистывания, если экран ещё не измерен. */
private const val DEFAULT_TRANSITION_DISTANCE_FALLBACK_PX: Int = 360

private fun String.withTrailingSlash(): String =
    if (endsWith("/")) this else "$this/"

/** Округляет индекс главы вниз и ограничивает границами spine. */
fun ReaderScreenSpec.clampChapterIndex(
    index: Int,
    spineLength: Int,
): Int {
    if (spineLength <= 0) {
        return 0
    }
    val floored = floor(index.toDouble()).toInt()
    return floored.coerceIn(0, spineLength - 1)
}

fun ReaderScreenSpec.inactiveLayerId(active: ReaderScreenSpec.ReaderLayerId): ReaderScreenSpec.ReaderLayerId =
    when (active) {
        ReaderScreenSpec.ReaderLayerId.A -> ReaderScreenSpec.ReaderLayerId.B
        ReaderScreenSpec.ReaderLayerId.B -> ReaderScreenSpec.ReaderLayerId.A
    }

fun ReaderScreenSpec.normalizeSavedScrollOffset(raw: Double?): Double {
    val v = raw ?: return 0.0
    return if (v.isFinite()) v else 0.0
}

fun ReaderScreenSpec.layerHtmlForWebView(html: String): String {
    val t = html.trim()
    return if (t.isNotEmpty()) t else ReaderScreenSpec.EMPTY_READER_HTML
}

fun ReaderScreenSpec.webViewBaseUrl(unpackedRootUri: String): String =
    unpackedRootUri.withTrailingSlash()

/** Предупреждать, если корень распаковки книги лежит вне каталога приложения. */
fun ReaderScreenSpec.shouldWarnUnpackedOutsideDocuments(
    unpackedRootUri: String,
    documentDirectory: String?,
): Boolean {
    val doc = documentDirectory ?: return false
    return !unpackedRootUri.withTrailingSlash().startsWith(doc.withTrailingSlash())
}

fun ReaderScreenSpec.layerToken(
    bookId: String,
    chapterIndex: Int,
    nowMillis: Long,
): String = "$bookId-$chapterIndex-$nowMillis"

fun ReaderScreenSpec.transitionDirectionSign(
    targetChapterIndex: Int,
    currentChapterIndex: Int,
): Int = if (targetChapterIndex > currentChapterIndex) 1 else -1

fun ReaderScreenSpec.targetChapterForPageTurn(
    currentChapterIndex: Int,
    spineLength: Int,
    direction: ReaderPageDirection,
): Int {
    val delta =
        when (direction) {
            ReaderPageDirection.NEXT -> 1
            ReaderPageDirection.PREV -> -1
        }
    return clampChapterIndex(currentChapterIndex + delta, spineLength)
}

@Suppress("LongParameterList")
fun ReaderScreenSpec.canAttemptChapterChange(
    epubNonNull: Boolean,
    spineLength: Int,
    phaseReady: Boolean,
    flipping: Boolean,
    currentLayerNonNull: Boolean,
): Boolean =
    epubNonNull && spineLength > 0 && phaseReady && !flipping && currentLayerNonNull

fun ReaderScreenSpec.shouldSkipChapterNavigation(
    clampedTargetIndex: Int,
    currentChapterIndex: Int,
): Boolean = clampedTargetIndex == currentChapterIndex

fun ReaderScreenSpec.transitionDistancePx(
    screenWidthPx: Int,
    fallbackWidthPx: Int = DEFAULT_TRANSITION_DISTANCE_FALLBACK_PX,
): Int = max(screenWidthPx, fallbackWidthPx)
