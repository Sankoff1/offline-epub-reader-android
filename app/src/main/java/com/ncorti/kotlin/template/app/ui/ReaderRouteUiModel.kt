package com.ncorti.kotlin.template.app.ui

import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.library.LibrarySessionState
import com.chitalka.storage.StorageService

internal data class ReaderRouteUiModel(
    val bookId: String,
    val bookPath: String,
    val persistence: LastOpenBookPersistence,
    val librarySession: LibrarySessionState,
    val storage: StorageService,
    val onPop: () -> Unit,
)
