package com.chitalka.ui.bookactions

import org.junit.Assert.assertEquals
import org.junit.Test

class BookActionsSheetSpecTest {

    @Test
    fun sheetBottomPadding_appliesMinInsetAndExtra() {
        assertEquals(20, BookActionsSheetSpec.sheetBottomPaddingDp(12))
        assertEquals(24, BookActionsSheetSpec.sheetBottomPaddingDp(16))
        assertEquals(20, BookActionsSheetSpec.sheetBottomPaddingDp(0))
    }

    @Test
    fun favoriteActionKey_picksByFavoriteFlag() {
        assertEquals(
            BookActionsSheetSpec.I18nKeys.ADD_TO_FAVORITES,
            BookActionsSheetSpec.favoriteActionKey(isFavorite = false),
        )
        assertEquals(
            BookActionsSheetSpec.I18nKeys.REMOVE_FROM_FAVORITES,
            BookActionsSheetSpec.favoriteActionKey(isFavorite = true),
        )
    }
}
