package com.ncorti.kotlin.template.app.ui.reader

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

/**
 * Реализация `window.ReactNativeWebView.postMessage` поверх нативного [android.webkit.WebView].
 * Страница читалки (немодифицируемый html) шлёт сообщения именно по этому имени —
 * это контракт со страницей, переименовать нельзя.
 */
class ReactNativeWebPolyfill(
    private val onMessage: (String) -> Unit,
) {
    private val main = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun postMessage(message: String) {
        main.post { onMessage(message) }
    }
}
