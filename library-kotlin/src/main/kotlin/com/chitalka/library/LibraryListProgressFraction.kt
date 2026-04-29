package com.chitalka.library

import kotlin.math.max
import kotlin.math.min

/**
 * Доля прочитанного для списка библиотеки: (индекс главы + доля прокрутки в главе) / всего глав.
 * Индекс главы 0-based. [scrollRangeMax] — максимальный scrollTop по текущей главе (высота − viewport).
 */
fun libraryListProgressFraction(
    totalChapters: Int,
    lastChapterIndex: Int,
    scrollOffset: Double,
    scrollRangeMax: Double,
): Double? {
    if (totalChapters <= 0) return null
    val idx = lastChapterIndex.coerceIn(0, totalChapters - 1)
    val chapterFrac =
        if (scrollRangeMax > 0.0 && scrollOffset.isFinite() && scrollRangeMax.isFinite()) {
            (scrollOffset / scrollRangeMax).coerceIn(0.0, 1.0)
        } else {
            0.0
        }
    val raw = (idx.toDouble() + chapterFrac) / totalChapters.toDouble()
    return min(1.0, max(0.0, raw))
}
