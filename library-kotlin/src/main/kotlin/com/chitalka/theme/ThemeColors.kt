package com.chitalka.theme

/** Режим темы — светлый или тёмный. */
enum class ThemeMode(val code: String) {
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromCode(code: String): ThemeMode? =
            entries.find { it.code == code }
    }
}

/** Палитра темы в виде hex-строк (#RRGGBB). */
data class ThemeColors(
    /** Основной задник экрана */
    val background: String,
    /** Кликабельные элементы (кнопки, акценты) */
    val interactive: String,
    /** Верхняя панель (header) */
    val topBar: String,
    /** Фон бокового меню */
    val menuBackground: String,
    /** Текст на топ-баре */
    val topBarText: String,
    /** Основной текст контента */
    val text: String,
    /** Второстепенный текст */
    val textSecondary: String,
)

val lightThemeColors = ThemeColors(
    background = "#F4F7EE",
    interactive = "#4CAF6B",
    topBar = "#2E7D4A",
    menuBackground = "#FFFFFF",
    topBarText = "#FFFFFF",
    text = "#1F2A1F",
    textSecondary = "#5C6F5E",
)

val darkThemeColors = ThemeColors(
    background = "#121814",
    interactive = "#7BC68C",
    topBar = "#2A6940",
    menuBackground = "#1C241E",
    topBarText = "#E8F5EA",
    text = "#E8EFE5",
    textSecondary = "#98AA9B",
)

fun getColorsForMode(mode: ThemeMode): ThemeColors =
    when (mode) {
        ThemeMode.DARK -> darkThemeColors
        ThemeMode.LIGHT -> lightThemeColors
    }
