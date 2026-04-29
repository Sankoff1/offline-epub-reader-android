package com.chitalka.screens.debuglogs

import java.time.Instant

/** Контракт экрана отладочных логов: i18n-ключи, экспорт, размеры. */
object DebugLogsScreenSpec {

    object I18nKeys {
        const val TITLE = "debugLogs.title"
        const val SUBTITLE = "debugLogs.subtitle"
        const val CLEAR = "debugLogs.clear"
        const val COPY = "debugLogs.copy"
        const val EXPORT = "debugLogs.export"
        const val EMPTY = "debugLogs.empty"
        const val EXPORT_DIALOG_TITLE = "debugLogs.exportDialogTitle"
    }

    const val EXPORT_MIME_TYPE: String = "text/plain"

    /** Стабильный ключ строки в LazyColumn: `ts-index`. */
    fun listItemKey(
        ts: Long,
        index: Int,
    ): String = "$ts-$index"

    /** Кнопки панели (очистить / скопировать / экспорт) неактивны при экспорте или пустом буфере. */
    fun toolbarActionsDisabled(
        exporting: Boolean,
        entryCount: Int,
    ): Boolean = exporting || entryCount == 0

    /** Имя файла экспорта вида `chitalka-logs-<ISO>.txt`, где `:` и `.` заменены на `-`. */
    fun exportFileName(now: Instant = Instant.now()): String {
        val iso = now.toString()
        val safe = iso.replace(":", "-").replace(".", "-")
        return "chitalka-logs-$safe.txt"
    }

    /** Конкатенация `cacheDir` + имя файла; гарантирует завершающий слэш у каталога. */
    fun exportFilePathInCache(
        cacheDir: String,
        fileName: String,
    ): String {
        val base =
            if (cacheDir.endsWith("/")) {
                cacheDir
            } else {
                "$cacheDir/"
            }
        return "$base$fileName"
    }
}
