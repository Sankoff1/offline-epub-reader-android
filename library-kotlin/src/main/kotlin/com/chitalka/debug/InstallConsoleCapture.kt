@file:Suppress("MagicNumber")
package com.chitalka.debug

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean

private val installed = AtomicBoolean(false)
private var savedSystemOut: PrintStream? = null
private var savedSystemErr: PrintStream? = null

/**
 * Перенаправляет [System.out] / [System.err] в [debugLogAppend]. Идемпотентно.
 *
 * Сообщения из [android.util.Log] сюда **не** попадают — только stdout/stderr
 * (например `println`, логи из нативных библиотек в stderr).
 *
 * Вызывать из `Application.onCreate`.
 */
fun installConsoleCapture() {
    if (!installed.compareAndSet(false, true)) {
        return
    }
    val out = System.out
    val err = System.err
    savedSystemOut = out
    savedSystemErr = err
    System.setOut(
        PrintStream(
            LineCaptureOutputStream(out, DebugLogLevel.Log),
            true,
            StandardCharsets.UTF_8,
        ),
    )
    System.setErr(
        PrintStream(
            LineCaptureOutputStream(err, DebugLogLevel.Error),
            true,
            StandardCharsets.UTF_8,
        ),
    )
}

/** Только для тестов: восстановить потоки и снять флаг установки. */
internal fun resetConsoleCaptureForTests() {
    savedSystemOut?.let { System.setOut(it) }
    savedSystemErr?.let { System.setErr(it) }
    savedSystemOut = null
    savedSystemErr = null
    installed.set(false)
}

private class LineCaptureOutputStream(
    private val downstream: PrintStream,
    private val level: DebugLogLevel,
) : OutputStream() {
    private val lineBuffer = ByteArrayOutputStream()

    override fun write(b: Int) {
        downstream.write(b)
        val u = b and 0xff
        when (u) {
            '\r'.code -> Unit
            '\n'.code -> emitBufferedAsMessage()
            else -> lineBuffer.write(u)
        }
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        downstream.write(b, off, len)
        for (i in off until off + len) {
            val u = b[i].toInt() and 0xff
            when (u) {
                '\r'.code -> Unit
                '\n'.code -> emitBufferedAsMessage()
                else -> lineBuffer.write(u)
            }
        }
    }

    override fun flush() {
        if (lineBuffer.size() > 0) {
            emitBufferedAsMessage()
        }
        downstream.flush()
    }

    private fun emitBufferedAsMessage() {
        if (lineBuffer.size() == 0) {
            return
        }
        val text = String(lineBuffer.toByteArray(), StandardCharsets.UTF_8).trimEnd('\r')
        lineBuffer.reset()
        if (text.isNotEmpty()) {
            try {
                debugLogAppend(level, text)
            } catch (e: Exception) {
                // защита от рекурсии: мы и есть перенаправление stdout/stderr → debugLogAppend.
                // Если он упадёт и мы напишем через downstream PrintStream, это снова проедет через
                // наш же write() и зациклится. Поэтому пишем в исходный сохранённый поток напрямую,
                // в обход нашей обёртки.
                savedSystemErr?.println(
                    "InstallConsoleCapture: debugLogAppend threw " +
                        "${e.javaClass.simpleName}: ${e.message}",
                )
            }
        }
    }

}
