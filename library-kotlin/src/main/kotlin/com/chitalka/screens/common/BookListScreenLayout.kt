package com.chitalka.screens.common

/** Общая вёрстка списка + FAB для экранов «Читаю сейчас», «Книги и документы», «Избранное». */
object BookListScreenLayout {
    private const val FAB_SIZE_DP: Int = 56
    private const val FAB_BOTTOM_INSET_BASE_DP: Int = 16
    private const val FAB_LIST_EXTRA_GAP_DP: Int = 16

    fun listContentBottomPaddingDp(safeInsetBottomDp: Int): Int =
        safeInsetBottomDp + FAB_BOTTOM_INSET_BASE_DP + FAB_SIZE_DP + FAB_LIST_EXTRA_GAP_DP
}
