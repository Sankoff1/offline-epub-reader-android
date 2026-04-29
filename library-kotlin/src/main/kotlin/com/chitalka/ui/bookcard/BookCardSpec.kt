package com.chitalka.ui.bookcard

import kotlin.math.roundToInt

/** Контракт карточки книги: вёрстка, прогресс чтения, i18n-ключи. */
object BookCardSpec {

    private const val PERCENT_SCALE: Double = 100.0

    object I18nKeys {
        const val READ_PERCENT = "books.readPercent"
        const val A11Y_OPEN_MENU = "a11y.openMenu"
    }

    object Layout {
        const val CARD_MARGIN_BOTTOM_DP: Int = 12
        const val CARD_PADDING_DP: Int = 12
        const val ROW_GAP_DP: Int = 12
        const val COVER_WIDTH_DP: Int = 72
        const val MENU_BUTTON_SIZE_DP: Int = 32
        const val PROGRESS_ROW_MARGIN_TOP_DP: Int = 6
        const val PROGRESS_ROW_GAP_DP: Int = 4

        private const val COVER_HEIGHT_TO_WIDTH_NUM: Int = 145
        private const val COVER_HEIGHT_TO_WIDTH_DEN: Int = 100

        fun coverHeightDp(): Int = COVER_WIDTH_DP * COVER_HEIGHT_TO_WIDTH_NUM / COVER_HEIGHT_TO_WIDTH_DEN
    }

    /** Ограничение доли прогресса диапазоном 0..1; нечисловые значения → 0. */
    fun clampProgressFraction(progress: Double): Double {
        if (!progress.isFinite()) {
            return 0.0
        }
        return progress.coerceIn(0.0, 1.0)
    }

    /** Показывать ли блок прогресса (любое не-null значение, включая NaN, считается «есть»). */
    fun hasProgressValue(progress: Double?): Boolean = progress != null

    fun progressPercentRounded(progress: Double): Int =
        (clampProgressFraction(progress) * PERCENT_SCALE).roundToInt()
}
