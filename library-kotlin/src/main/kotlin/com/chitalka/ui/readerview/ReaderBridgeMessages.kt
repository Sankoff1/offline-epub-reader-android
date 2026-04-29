package com.chitalka.ui.readerview

import com.chitalka.debug.DebugLogLevel
import com.chitalka.debug.debugLogAppend
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private const val BRIDGE_PARSE_LOG_PREFIX_LIMIT = 80

/** Throttle обработки scroll-сообщений в нативе после получения из WebView. */
const val READER_BRIDGE_SCROLL_DEBOUNCE_MS: Long = 350L

private val bridgeJson = Json { ignoreUnknownKeys = true }

/**
 * Исходящие сообщения (Kotlin → WebView). Сериализуются в JSON и пушатся
 * со стороны нативного слоя через `evaluateJavascript("window.__chitalkaApplyTheme(...)")`
 * без перезагрузки страницы.
 */
sealed interface ReaderBridgeOutboundMessage {
    /**
     * Применить новую палитру темы к открытой главе. Цвета — `#RRGGBB`.
     */
    data class ApplyTheme(
        val backgroundHex: String,
        val foregroundHex: String,
        val isDark: Boolean,
    ) : ReaderBridgeOutboundMessage
}

/**
 * Стабильный JSON-формат outbound-сообщения. Поля и регистр фиксированы — JS-обработчик
 * `__chitalkaApplyTheme` в `injectedScrollBridge.js` ожидает именно их.
 */
fun encodeReaderBridgeOutboundMessage(msg: ReaderBridgeOutboundMessage): String =
    when (msg) {
        is ReaderBridgeOutboundMessage.ApplyTheme ->
            buildJsonObject {
                put("type", "ApplyTheme")
                put("background", msg.backgroundHex)
                put("foreground", msg.foregroundHex)
                put("isDark", msg.isDark)
            }.toString()
    }

sealed class ReaderBridgeInboundMessage {
    /**
     * @param scrollRangeMax макс. scrollTop по документу главы; null — не обновлять сохранённый max (старые сообщения).
     */
    data class Scroll(
        val y: Double,
        val scrollRangeMax: Double?,
    ) : ReaderBridgeInboundMessage()

    data class Page(
        val direction: ReaderPageDirection,
    ) : ReaderBridgeInboundMessage()

    data object Ready : ReaderBridgeInboundMessage()
}

/**
 * Разбор JSON-сообщения из WebView в типизированное событие моста.
 * Возвращает `null` на любое не-наше или повреждённое сообщение — это часть контракта,
 * вызывающий не должен ронять WebView из-за случайного `postMessage` от стороннего скрипта.
 */
fun parseReaderBridgeInboundMessage(json: String): ReaderBridgeInboundMessage? =
    try {
        val obj = bridgeJson.parseToJsonElement(json).jsonObject
        val t = obj["t"]?.jsonPrimitive?.content ?: return null
        when (t) {
            "scroll" -> {
                val prim = obj["y"]?.jsonPrimitive ?: return null
                val y = prim.doubleFromBridge() ?: return null
                if (!y.isFinite()) return null
                val yMaxPrim = obj["yMax"]?.jsonPrimitive
                val yMax =
                    yMaxPrim
                        ?.doubleFromBridge()
                        ?.takeIf { it.isFinite() && it >= 0.0 }
                ReaderBridgeInboundMessage.Scroll(y, yMax)
            }
            "page" -> {
                val dir = obj["dir"]?.jsonPrimitive?.content ?: return null
                val d = ReaderPageDirection.fromWire(dir) ?: return null
                ReaderBridgeInboundMessage.Page(d)
            }
            "ready" -> ReaderBridgeInboundMessage.Ready
            else -> null
        }
    } catch (e: Exception) {
        // контракт допускает любые сторонние postMessage, поэтому возвращаем null;
        // но падение парсера при наших же сообщениях маскировать тихо нельзя — логируем.
        val prefix = json.take(BRIDGE_PARSE_LOG_PREFIX_LIMIT)
        debugLogAppend(
            DebugLogLevel.Warn,
            "parseReaderBridgeInboundMessage: parse failed for json=\"$prefix\" " +
                "(${e.javaClass.simpleName}: ${e.message})",
        )
        null
    }

private fun JsonPrimitive.doubleFromBridge(): Double? =
    content.toDoubleOrNull()
