@file:Suppress("MatchingDeclarationName")
package com.chitalka.i18n

/** Поддерживаемые языки приложения. */
enum class AppLocale(val code: String) {
    RU("ru"),
    EN("en"),
    ;

    companion object {
        fun fromCode(code: String): AppLocale? = entries.find { it.code == code }
    }
}

/** Порядок локалей в UI выбора языка. */
val APP_LOCALES: List<AppLocale> = listOf(AppLocale.RU, AppLocale.EN)

const val LOCALE_STORAGE_KEY = "chitalka_locale"
