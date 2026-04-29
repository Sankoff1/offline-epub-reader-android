package com.ncorti.kotlin.template.app.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.chitalka.debug.DebugLogLevel
import com.chitalka.debug.debugLogAppend
import com.chitalka.ui.readerview.readerInjectedScrollBridge
import com.chitalka.ui.readerview.readerLoadEndScrollAndReadyScript

/** JS-имя моста: жёстко завязано на html страницы читалки, переименовать нельзя. */
internal const val READER_JS_INTERFACE_NAME = "ReactNativeWebView"

/**
 * Создаёт и сразу настраивает WebView для одной главы: включает JS, ставит chrome/web-клиентов,
 * подключает [jsInterface] под именем `ReactNativeWebView` и грузит [displayHtml].
 * Все скрипты моста (см. `readerInjectedScrollBridge` / `readerLoadEndScrollAndReadyScript`)
 * исполняются в `onPageFinished` — это часть контракта со страницей.
 */
@SuppressLint("SetJavaScriptEnabled")
internal fun createReaderWebView(
    context: Context,
    jsInterface: ReactNativeWebPolyfill,
    baseUrl: String,
    displayHtml: String,
    initialScrollY: Double,
): WebView {
    val bridgeScript = readerInjectedScrollBridge()
    val scrollReadyScript = readerLoadEndScrollAndReadyScript(initialScrollY)

    return WebView(context).apply {
        layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        @Suppress("DEPRECATION")
        settings.allowFileAccess = true
        addJavascriptInterface(jsInterface, READER_JS_INTERFACE_NAME)
        webChromeClient = ReaderConsoleChromeClient
        webViewClient =
            object : WebViewClient() {
                override fun onPageFinished(
                    view: WebView?,
                    url: String?,
                ) {
                    view?.evaluateJavascript(bridgeScript, null)
                    view?.evaluateJavascript(scrollReadyScript, null)
                }
            }
        loadDataWithBaseURL(
            baseUrl,
            displayHtml,
            "text/html",
            Charsets.UTF_8.name(),
            null,
        )
    }
}

/**
 * Дублирует `console.*` со страницы читалки в общий debug-лог.
 * Без состояния — один объект на все WebView.
 */
private object ReaderConsoleChromeClient : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        val cm = consoleMessage ?: return false
        val src = cm.sourceId().orEmpty()
        val loc =
            if (cm.lineNumber() > 0) {
                "${if (src.isNotEmpty()) "$src:" else ""}${cm.lineNumber()} "
            } else {
                if (src.isNotEmpty()) "$src " else ""
            }
        val line = "$loc${cm.message()}".trim()
        val level =
            when (cm.messageLevel()) {
                ConsoleMessage.MessageLevel.ERROR -> DebugLogLevel.Error
                ConsoleMessage.MessageLevel.WARNING -> DebugLogLevel.Warn
                ConsoleMessage.MessageLevel.DEBUG,
                ConsoleMessage.MessageLevel.TIP,
                -> DebugLogLevel.Debug
                else -> DebugLogLevel.Log
            }
        debugLogAppend(level, "[WebView] $line")
        return true
    }
}
