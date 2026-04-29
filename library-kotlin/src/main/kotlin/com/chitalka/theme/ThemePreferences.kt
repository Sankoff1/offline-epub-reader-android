package com.chitalka.theme

import com.chitalka.debug.DebugLogLevel
import com.chitalka.debug.debugLogAppend
import com.chitalka.library.LastOpenBookPersistence

/** Ключ хранилища для режима темы. */
const val THEME_MODE_STORAGE_KEY = "chitalka_theme_mode"

/** Снимок для UI: режим и палитра. Палитры статичны (см. [getColorsForMode]) — кэшировать не нужно. */
data class ThemeUiState(
    val mode: ThemeMode,
) {
    val colors: ThemeColors
        get() = getColorsForMode(mode)
}

/** Чтение сохранённого режима. Невалидное / отсутствующее → `null` (вызывающий применит дефолт). */
suspend fun loadPersistedThemeMode(storage: LastOpenBookPersistence): ThemeMode? =
    try {
        val stored = storage.getItem(THEME_MODE_STORAGE_KEY) ?: return null
        ThemeMode.fromCode(stored)
    } catch (e: Exception) {
        // best-effort: применим системный дефолт; факт сбоя пригодится при отладке.
        debugLogAppend(
            DebugLogLevel.Warn,
            "loadPersistedThemeMode: read failed for key=$THEME_MODE_STORAGE_KEY " +
                "(${e.javaClass.simpleName}: ${e.message})",
        )
        null
    }

/** Сохранение режима; ошибки логируются — потеря настройки не критична, но виден сам факт. */
suspend fun persistThemeMode(storage: LastOpenBookPersistence, mode: ThemeMode) {
    try {
        storage.setItem(THEME_MODE_STORAGE_KEY, mode.code)
    } catch (e: Exception) {
        debugLogAppend(
            DebugLogLevel.Warn,
            "persistThemeMode: write failed for key=$THEME_MODE_STORAGE_KEY value=${mode.code} " +
                "(${e.javaClass.simpleName}: ${e.message})",
        )
    }
}

/** Переключение light ↔ dark. */
fun ThemeMode.toggle(): ThemeMode =
    when (this) {
        ThemeMode.LIGHT -> ThemeMode.DARK
        ThemeMode.DARK -> ThemeMode.LIGHT
    }

/** Переключить режим и сохранить. Возвращает новый режим. */
suspend fun togglePersistedThemeMode(storage: LastOpenBookPersistence, current: ThemeMode): ThemeMode {
    val next = current.toggle()
    persistThemeMode(storage, next)
    return next
}
