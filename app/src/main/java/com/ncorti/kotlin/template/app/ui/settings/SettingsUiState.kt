package com.ncorti.kotlin.template.app.ui.settings

import androidx.compose.runtime.Immutable
import com.chitalka.i18n.AppLocale
import com.chitalka.theme.ThemeMode

/**
 * Состояние экрана настроек. Глобальная локаль/тема живут в [com.ncorti.kotlin.template.app.ui.ChitalkaApp]
 * и приходят через CompositionLocal — VM их не владеет, а лишь повторяет последние принятые значения,
 * чтобы рендер был полностью отвязан от внешних параметров pane.
 */
@Immutable
data class SettingsUiState(
    val locale: AppLocale,
    val themeMode: ThemeMode,
    val isPersistingLocale: Boolean,
    val isPersistingThemeMode: Boolean,
) {
    companion object {
        fun initial(locale: AppLocale, themeMode: ThemeMode): SettingsUiState =
            SettingsUiState(
                locale = locale,
                themeMode = themeMode,
                isPersistingLocale = false,
                isPersistingThemeMode = false,
            )
    }
}
