package com.chitalka.navigation

/** Экраны бокового меню. У маршрутов нет параметров. */
enum class DrawerScreen(val routeName: String) {
    ReadingNow("ReadingNow"),
    BooksAndDocs("BooksAndDocs"),
    Favorites("Favorites"),
    Cart("Cart"),
    DebugLogs("DebugLogs"),
    Settings("Settings"),
    ;

    companion object {
        fun fromRouteName(routeName: String): DrawerScreen? =
            entries.find { it.routeName == routeName }
    }
}

/** Имена маршрутов корневого стека. */
object RootStackRoutes {
    const val MAIN = "Main"
    const val READER = "Reader"
}

/** Аргументы экрана читалки. */
data class ReaderRouteParams(
    val bookPath: String,
    val bookId: String,
)
