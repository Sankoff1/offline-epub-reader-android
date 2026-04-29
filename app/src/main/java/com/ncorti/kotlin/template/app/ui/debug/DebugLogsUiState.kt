package com.ncorti.kotlin.template.app.ui.debug

import androidx.compose.runtime.Immutable
import com.chitalka.debug.DebugLogEntry

/** Состояние экрана отладочных логов. */
@Immutable
data class DebugLogsUiState(
    val entries: List<DebugLogEntry>,
    val exporting: Boolean,
) {
    val isEmpty: Boolean get() = entries.isEmpty()

    companion object {
        val Initial: DebugLogsUiState = DebugLogsUiState(
            entries = emptyList(),
            exporting = false,
        )
    }
}
