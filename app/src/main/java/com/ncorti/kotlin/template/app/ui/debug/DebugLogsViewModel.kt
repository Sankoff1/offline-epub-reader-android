package com.ncorti.kotlin.template.app.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chitalka.debug.debugLogClear
import com.chitalka.debug.debugLogGetSnapshot
import com.chitalka.debug.debugLogSubscribe
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Коалесценция нужна, чтобы шторм `console.*` из WebView не вызывал `reload()` на каждую строку.
 */
private const val DEBUG_LOG_RELOAD_COALESCE_MS = 120L

class DebugLogsViewModel : ViewModel() {

    private val _state = MutableStateFlow(DebugLogsUiState.Initial)
    val state: StateFlow<DebugLogsUiState> = _state.asStateFlow()

    private val pendingReload = AtomicBoolean(false)
    private val unsubscribe: () -> Unit

    init {
        reload()
        unsubscribe = debugLogSubscribe { scheduleReload() }
    }

    fun onClear() {
        debugLogClear()
        reload()
    }

    fun onExportingChange(exporting: Boolean) {
        _state.update { it.copy(exporting = exporting) }
    }

    override fun onCleared() {
        unsubscribe()
    }

    private fun scheduleReload() {
        if (pendingReload.compareAndSet(false, true)) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(DEBUG_LOG_RELOAD_COALESCE_MS)
                pendingReload.set(false)
                reload()
            }
        }
    }

    private fun reload() {
        val snap = debugLogGetSnapshot()
        _state.update { it.copy(entries = snap) }
    }
}
