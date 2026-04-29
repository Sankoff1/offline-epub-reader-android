package com.chitalka.ui.bookactions

import kotlin.math.max

/** Контракт нижнего листа действий с книгой. */
object BookActionsSheetSpec {

    object I18nKeys {
        const val SHEET_TITLE = "bookActions.title"
        const val ADD_TO_FAVORITES = "bookActions.addToFavorites"
        const val REMOVE_FROM_FAVORITES = "bookActions.removeFromFavorites"
        const val MOVE_TO_TRASH = "bookActions.moveToTrash"
        const val COMMON_CANCEL = "common.cancel"
    }

    private const val SHEET_BOTTOM_PADDING_MIN_INSET_DP: Int = 12
    private const val SHEET_BOTTOM_PADDING_EXTRA_DP: Int = 8

    /** Нижний отступ листа: max(insets.bottom, минимум) + хвостовой extra. */
    fun sheetBottomPaddingDp(safeInsetBottomDp: Int): Int =
        max(safeInsetBottomDp, SHEET_BOTTOM_PADDING_MIN_INSET_DP) + SHEET_BOTTOM_PADDING_EXTRA_DP

    /** Ключ строки действия «избранное»: добавить или убрать. */
    fun favoriteActionKey(isFavorite: Boolean): String =
        if (isFavorite) I18nKeys.REMOVE_FROM_FAVORITES else I18nKeys.ADD_TO_FAVORITES
}
