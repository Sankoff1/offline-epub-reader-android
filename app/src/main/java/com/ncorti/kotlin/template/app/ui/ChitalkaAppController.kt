package com.ncorti.kotlin.template.app.ui

import com.chitalka.navigation.ReaderNavCoordinator

/**
 * Навигация в читалку (через [ReaderNavCoordinator], пока NavHost не готов) и сигнал обновления списков.
 */
class ChitalkaAppController(
    private val readerNavCoordinator: ReaderNavCoordinator,
    private val onListsChanged: () -> Unit,
) {
    fun bumpLists() {
        onListsChanged()
    }

    fun openReader(
        bookId: String,
        bookPath: String,
    ) {
        readerNavCoordinator.navigateToReader(bookPath = bookPath, bookId = bookId)
    }
}
