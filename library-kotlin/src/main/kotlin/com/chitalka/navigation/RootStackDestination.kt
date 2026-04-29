package com.chitalka.navigation

/**
 * Корневой стек экранов: drawer-обёртка [Main] и [Reader] поверх.
 */
sealed interface RootStackDestination {
    data object Main : RootStackDestination

    data class Reader(
        val params: ReaderRouteParams,
    ) : RootStackDestination
}

/** Имя маршрута в графе навигации. */
val RootStackDestination.routeId: String
    get() =
        when (this) {
            is RootStackDestination.Main -> RootStackRoutes.MAIN
            is RootStackDestination.Reader -> RootStackRoutes.READER
        }

fun readerRootDestination(
    bookPath: String,
    bookId: String,
): RootStackDestination.Reader =
    RootStackDestination.Reader(ReaderRouteParams(bookPath = bookPath, bookId = bookId))
