package com.chitalka.debug

import java.time.Instant

/** Уровень строки лога. */
enum class DebugLogLevel {
    Log,
    Warn,
    Error,
    Debug,
    Info,
    ;

    /** Имя уровня в нижнем регистре для сериализации и UI: `log`, `warn`, … */
    val wireName: String get() = name.lowercase()
}

/** Одна запись буфера. */
data class DebugLogEntry(
    val ts: Long,
    val level: DebugLogLevel,
    val message: String,
)

private const val MAX_ENTRIES = 4000
private const val EXPORT_HEADER_RULE_LENGTH = 40
private const val EXPORT_LINES_INITIAL_EXTRA = 8

private val lock = Any()
private val entries = ArrayDeque<DebugLogEntry>(MAX_ENTRIES + 1)
private val listeners = mutableSetOf<() -> Unit>()

private fun notifyListeners() {
    val snapshot = synchronized(lock) { listeners.toList() }
    for (fn in snapshot) {
        try {
            fn()
        } catch (e: Exception) {
            // защита от рекурсии при падении канала записи логов: вызывать debugLogAppend
            // нельзя — подписчик может сам быть логгером, и мы зациклимся. Поэтому fallback
            // прямо в System.err с тем же контекстом.
            System.err.println(
                "DebugLog.notifyListeners: listener threw " +
                    "${e.javaClass.simpleName}: ${e.message}",
            )
        }
    }
}

/** Добавить запись в кольцевой буфер логов и уведомить подписчиков. */
fun debugLogAppend(level: DebugLogLevel, message: String) {
    synchronized(lock) {
        entries.addLast(DebugLogEntry(ts = System.currentTimeMillis(), level = level, message = message))
        while (entries.size > MAX_ENTRIES) {
            entries.removeFirst()
        }
    }
    notifyListeners()
}

/** Подписаться на изменения буфера; возвращает функцию отписки. */
fun debugLogSubscribe(listener: () -> Unit): () -> Unit {
    synchronized(lock) {
        listeners.add(listener)
    }
    return {
        synchronized(lock) {
            listeners.remove(listener)
        }
    }
}

/** Снимок текущего буфера. */
fun debugLogGetSnapshot(): List<DebugLogEntry> = synchronized(lock) { entries.toList() }

/** Очистить буфер логов. */
fun debugLogClear() {
    synchronized(lock) {
        entries.clear()
    }
    notifyListeners()
}

/** Текстовое представление буфера для экспорта/копирования. */
fun debugLogFormatExport(): String {
    val currentEntries = debugLogGetSnapshot()
    val lines = ArrayList<String>(currentEntries.size + EXPORT_LINES_INITIAL_EXTRA)
    lines += "Chitalka debug log export"
    lines += "Generated: ${Instant.now()}"
    lines += "Entries: ${currentEntries.size}"
    lines += "—".repeat(EXPORT_HEADER_RULE_LENGTH)
    lines += ""
    for (e in currentEntries) {
        lines += "${Instant.ofEpochMilli(e.ts)}\t[${e.level.wireName}]\t${e.message}"
    }
    return lines.joinToString("\n")
}
