package com.chitalka.debug

import android.util.Log

/**
 * Дублирует вызовы [android.util.Log] в буфер [debugLogAppend], чтобы строки появлялись
 * во вкладке «Отладочные логи». Сам [installConsoleCapture] перехватывает только stdout/stderr;
 * на Android [Log] пишет в logcat и мимо System.out.
 */
object ChitalkaMirrorLog {
    private fun mirror(level: DebugLogLevel, tag: String, message: String) {
        debugLogAppend(level, "$tag: $message")
    }

    fun v(tag: String, message: String) {
        Log.v(tag, message)
        mirror(DebugLogLevel.Debug, tag, message)
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        mirror(DebugLogLevel.Debug, tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        mirror(DebugLogLevel.Info, tag, message)
    }

    fun w(tag: String, message: String, tr: Throwable? = null) {
        if (tr != null) {
            Log.w(tag, message, tr)
            mirror(DebugLogLevel.Warn, tag, "$message\n${Log.getStackTraceString(tr)}")
        } else {
            Log.w(tag, message)
            mirror(DebugLogLevel.Warn, tag, message)
        }
    }

    fun e(tag: String, message: String, tr: Throwable? = null) {
        if (tr != null) {
            Log.e(tag, message, tr)
            mirror(DebugLogLevel.Error, tag, "$message\n${Log.getStackTraceString(tr)}")
        } else {
            Log.e(tag, message)
            mirror(DebugLogLevel.Error, tag, message)
        }
    }
}
