@file:Suppress("LongParameterList")

package com.ncorti.kotlin.template.app.ui.reader

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode
import com.chitalka.ui.readerview.ReaderBridgeInboundMessage
import com.chitalka.ui.readerview.ReaderBridgeOutboundMessage
import com.chitalka.ui.readerview.encodeReaderBridgeOutboundMessage
import com.chitalka.ui.readerview.injectDarkReaderHead
import com.chitalka.ui.readerview.parseReaderBridgeInboundMessage
import org.json.JSONObject

/**
 * WebView читалки одной главы. Идентичность WebView привязана к [chapterKey] и [html]:
 * пересоздаётся только при смене главы или содержимого. Тема меняется на лету через
 * outbound-канал моста (`__chitalkaApplyTheme`), запекается в HTML только для initial paint
 * (чтобы первая отрисовка не «вспыхивала» белым).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ChitalkaReaderWebView(
    chapterKey: String,
    html: String,
    baseUrl: String,
    initialScrollY: Double,
    themeMode: ThemeMode,
    themeColors: ThemeColors,
    /** Блокирует касания во время перелистывания и на неактивном слое. */
    interceptAllTouches: Boolean,
    onBridgeMessage: (ReaderBridgeInboundMessage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bridgeHandler = rememberUpdatedState(onBridgeMessage)

    // Initial paint: тема всегда запекается в HTML, чтобы первая отрисовка соответствовала
    // текущей палитре. Дальше переменные `--chitalka-bg/--chitalka-fg` живут через CSS,
    // их пушит `LaunchedEffect` ниже без перезагрузки страницы.
    val initialDisplayHtml =
        remember(chapterKey, html) {
            injectDarkReaderHead(html, themeColors)
        }

    key(chapterKey, baseUrl, html) {
        val jsInterface =
            remember {
                ReactNativeWebPolyfill { json ->
                    parseReaderBridgeInboundMessage(json)?.let { msg ->
                        bridgeHandler.value.invoke(msg)
                    }
                }
            }

        val webView =
            remember {
                createReaderWebView(
                    context = context,
                    jsInterface = jsInterface,
                    baseUrl = baseUrl,
                    displayHtml = initialDisplayHtml,
                    initialScrollY = initialScrollY,
                )
            }

        LaunchedEffect(webView, themeMode, themeColors) {
            val payload =
                encodeReaderBridgeOutboundMessage(
                    ReaderBridgeOutboundMessage.ApplyTheme(
                        backgroundHex = themeColors.background,
                        foregroundHex = themeColors.text,
                        isDark = themeMode == ThemeMode.DARK,
                    ),
                )
            // JSONObject.quote экранирует кавычки/слэши и оборачивает в "…" — безопасно
            // подставляется в JS-вызов.
            val quoted = JSONObject.quote(payload)
            webView.evaluateJavascript(
                "if (window.__chitalkaApplyTheme) { window.__chitalkaApplyTheme($quoted); }",
                null,
            )
        }

        DisposableEffect(webView) {
            onDispose {
                webView.stopLoading()
                webView.removeJavascriptInterface(READER_JS_INTERFACE_NAME)
                webView.webChromeClient = null
                webView.webViewClient = WebViewClient()
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.clearHistory()
                webView.destroy()
            }
        }

        AndroidView(
            modifier = modifier,
            factory = { webView },
            update = { wv ->
                @Suppress("ClickableViewAccessibility")
                wv.setOnTouchListener(
                    if (interceptAllTouches) {
                        View.OnTouchListener { _, _ -> true }
                    } else {
                        null
                    },
                )
            },
        )
    }
}
