package com.chitalka.navigation

/**
 * Параметры бокового меню без привязки к Compose.
 */
object DrawerNavigationSpec {
    /** Порядок пунктов в меню. */
    val drawerScreenOrder: List<DrawerScreen> =
        listOf(
            DrawerScreen.ReadingNow,
            DrawerScreen.BooksAndDocs,
            DrawerScreen.Favorites,
            DrawerScreen.Cart,
            DrawerScreen.DebugLogs,
            DrawerScreen.Settings,
        )
}

/** Ключ строки в Android-ресурсах (резолвится через `chitalkaString`) для подписи пункта меню. */
val DrawerScreen.drawerLabelI18nPath: String
    get() =
        when (this) {
            DrawerScreen.ReadingNow -> "drawer.readingNow"
            DrawerScreen.BooksAndDocs -> "drawer.books"
            DrawerScreen.Favorites -> "drawer.favorites"
            DrawerScreen.Cart -> "drawer.cart"
            DrawerScreen.DebugLogs -> "drawer.debugLogs"
            DrawerScreen.Settings -> "drawer.settings"
        }
