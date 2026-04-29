package com.ncorti.kotlin.template.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.persistLocale
import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.theme.ThemeMode
import com.chitalka.theme.persistThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Контроллер действий экрана настроек. Сам не владеет глобальной локалью/темой:
 * после успешной записи в persistence сообщает наверх через [onLocaleChanged] / [onThemeModeChanged],
 * а рут-composable обновляет своё `MutableState`, который читает CompositionLocal-провайдер.
 */
class SettingsViewModel(
    private val persistence: LastOpenBookPersistence,
    initialLocale: AppLocale,
    initialThemeMode: ThemeMode,
    private val onLocaleChanged: (AppLocale) -> Unit,
    private val onThemeModeChanged: (ThemeMode) -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState.initial(initialLocale, initialThemeMode))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    /** Синхронизация локального state'а VM, когда рут-composable получил новое значение извне. */
    fun onExternalLocaleChange(locale: AppLocale) {
        if (_state.value.locale == locale) return
        _state.update { it.copy(locale = locale) }
    }

    fun onExternalThemeModeChange(mode: ThemeMode) {
        if (_state.value.themeMode == mode) return
        _state.update { it.copy(themeMode = mode) }
    }

    fun onLocaleChange(locale: AppLocale) {
        if (_state.value.locale == locale) return
        _state.update { it.copy(isPersistingLocale = true) }
        viewModelScope.launch {
            persistLocale(persistence, locale)
            _state.update { it.copy(locale = locale, isPersistingLocale = false) }
            onLocaleChanged(locale)
        }
    }

    fun onThemeModeChange(mode: ThemeMode) {
        if (_state.value.themeMode == mode) return
        _state.update { it.copy(isPersistingThemeMode = true) }
        viewModelScope.launch {
            persistThemeMode(persistence, mode)
            _state.update { it.copy(themeMode = mode, isPersistingThemeMode = false) }
            onThemeModeChanged(mode)
        }
    }

    class Factory(
        private val persistence: LastOpenBookPersistence,
        private val initialLocale: AppLocale,
        private val initialThemeMode: ThemeMode,
        private val onLocaleChanged: (AppLocale) -> Unit,
        private val onThemeModeChanged: (ThemeMode) -> Unit,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                "Unknown ViewModel class: $modelClass"
            }
            return SettingsViewModel(
                persistence = persistence,
                initialLocale = initialLocale,
                initialThemeMode = initialThemeMode,
                onLocaleChanged = onLocaleChanged,
                onThemeModeChanged = onThemeModeChanged,
            ) as T
        }
    }
}
