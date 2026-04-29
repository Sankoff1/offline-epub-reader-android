@file:Suppress("MatchingDeclarationName")

package com.chitalka.i18n

import com.chitalka.debug.DebugLogLevel
import com.chitalka.debug.debugLogAppend
import com.chitalka.library.LastOpenBookPersistence

/**
 * Чтение сохранённой локали. Невалидное или отсутствующее значение → `null`
 * (вызывающий применит дефолт).
 */
suspend fun loadPersistedLocale(storage: LastOpenBookPersistence): AppLocale? =
    try {
        val stored = storage.getItem(LOCALE_STORAGE_KEY) ?: return null
        AppLocale.fromCode(stored)
    } catch (e: Exception) {
        // best-effort: применим системный дефолт; но факт сбоя видеть полезно при отладке.
        debugLogAppend(
            DebugLogLevel.Warn,
            "loadPersistedLocale: read failed for key=$LOCALE_STORAGE_KEY " +
                "(${e.javaClass.simpleName}: ${e.message})",
        )
        null
    }

/** Сохранение локали; ошибки логируются — выбор языка не критичен, но факт сбоя важен. */
suspend fun persistLocale(storage: LastOpenBookPersistence, locale: AppLocale) {
    try {
        storage.setItem(LOCALE_STORAGE_KEY, locale.code)
    } catch (e: Exception) {
        debugLogAppend(
            DebugLogLevel.Warn,
            "persistLocale: write failed for key=$LOCALE_STORAGE_KEY value=${locale.code} " +
                "(${e.javaClass.simpleName}: ${e.message})",
        )
    }
}
