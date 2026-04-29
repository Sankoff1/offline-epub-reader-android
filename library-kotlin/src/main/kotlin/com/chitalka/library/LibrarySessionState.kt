package com.chitalka.library

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Иммутабельный снимок состояния сессии библиотеки. */
data class LibrarySessionUiState(
    val libraryEpoch: Int = 0,
    val bookCount: Int = 0,
    val storageReady: Boolean = false,
    val isSearchOpen: Boolean = false,
    val searchQuery: String = "",
    val welcomeDismissedThisSession: Boolean = false,
    val welcomePickerHint: String? = null,
    val suppressWelcomeForPicker: Boolean = false,
) {
    /** Видимость welcome-модалки первого запуска. */
    val isFirstLaunchWelcomeVisible: Boolean
        get() = storageReady &&
            bookCount == 0 &&
            !welcomeDismissedThisSession &&
            !suppressWelcomeForPicker
}

/**
 * Состояние библиотеки на уровне приложения (счётчики, поиск, welcome-флаги).
 * Все мутации идут через [StateFlow] — потокобезопасно, наблюдаемо из Compose
 * через `collectAsStateWithLifecycle()`.
 */
@Suppress("TooManyFunctions")
class LibrarySessionState(initial: LibrarySessionUiState = LibrarySessionUiState()) {

    private val _state = MutableStateFlow(initial)
    val state: StateFlow<LibrarySessionUiState> = _state.asStateFlow()

    /** Увеличивается после импорта и др. — подписка UI на обновление списков. */
    fun bumpLibraryEpoch() {
        _state.update { it.copy(libraryEpoch = it.libraryEpoch + 1) }
    }

    fun markStorageReady(ready: Boolean = true) {
        _state.update { it.copy(storageReady = ready) }
    }

    fun updateBookCount(count: Long) {
        val clamped = count.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        _state.update { it.copy(bookCount = clamped) }
    }

    fun openSearch() {
        _state.update { it.copy(isSearchOpen = true) }
    }

    fun closeSearch() {
        _state.update { it.copy(isSearchOpen = false, searchQuery = "") }
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun dismissWelcomeModal() {
        _state.update { it.copy(welcomeDismissedThisSession = true, welcomePickerHint = null) }
    }

    fun setWelcomePickerHint(hint: String?) {
        _state.update { it.copy(welcomePickerHint = hint) }
    }

    fun setSuppressWelcomeForPicker(suppress: Boolean) {
        _state.update { it.copy(suppressWelcomeForPicker = suppress) }
    }
}
