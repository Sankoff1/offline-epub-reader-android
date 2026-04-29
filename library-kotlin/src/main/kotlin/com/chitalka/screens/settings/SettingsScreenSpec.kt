package com.chitalka.screens.settings

import com.chitalka.i18n.AppLocale
import kotlin.math.round

/** Контракт экрана настроек. */
object SettingsScreenSpec {

    /** Высота меню выбора языка (две строки по 48 + разделитель). */
    const val LANGUAGE_MENU_ESTIMATE_PX: Int = 97

    object I18nKeys {
        const val THEME_SECTION = "settings.themeSection"
        const val DARK_THEME = "settings.darkTheme"
        const val LANGUAGE_SECTION = "settings.languageSection"
        const val LANGUAGE_RU = "settings.languageRu"
        const val LANGUAGE_EN = "settings.languageEn"
        const val VERSION_LABEL = "settings.versionLabel"
    }

    /** Ключ строки для подписи пункта языка в выпадающем списке. */
    fun languageOptionKey(forLocale: AppLocale): String =
        when (forLocale) {
            AppLocale.RU -> I18nKeys.LANGUAGE_RU
            AppLocale.EN -> I18nKeys.LANGUAGE_EN
        }

    /** Показывать меню над якорём, если снизу не хватает места. */
    fun shouldOpenLanguageMenuAbove(
        windowHeightPx: Float,
        anchorY: Float,
        anchorHeight: Float,
    ): Boolean {
        val spaceBelow = windowHeightPx - (anchorY + anchorHeight)
        return spaceBelow < LANGUAGE_MENU_ESTIMATE_PX && anchorY > LANGUAGE_MENU_ESTIMATE_PX
    }

    /** Вертикальная позиция (top, в px) меню языка относительно якоря. */
    fun languageMenuTopPx(
        anchorY: Float,
        anchorHeight: Float,
        openAbove: Boolean,
    ): Float =
        if (openAbove) {
            round(anchorY) - LANGUAGE_MENU_ESTIMATE_PX + 1f
        } else {
            round(anchorY + anchorHeight) - 1f
        }
}
