package com.ncorti.kotlin.template.app.ui.library

import androidx.compose.runtime.Immutable
import com.chitalka.core.types.LibraryBookWithProgress

/** Цель открытого диалога окончательного удаления. */
@Immutable
data class TrashPurgeTarget(val book: LibraryBookWithProgress)

/** Состояние экрана корзины. Поиск приходит снаружи. */
@Immutable
data class TrashUiState(
    val rawBooks: List<LibraryBookWithProgress>,
    val filteredBooks: List<LibraryBookWithProgress>,
    val isLoading: Boolean,
    val hasActiveSearch: Boolean,
    val pendingPurge: TrashPurgeTarget?,
) {
    companion object {
        val Initial: TrashUiState =
            TrashUiState(
                rawBooks = emptyList(),
                filteredBooks = emptyList(),
                isLoading = true,
                hasActiveSearch = false,
                pendingPurge = null,
            )
    }
}
