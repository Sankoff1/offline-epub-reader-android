package com.chitalka.ui.readerview

import kotlin.math.floor
import kotlin.math.max

private const val RESOURCE_PATH = "/chitalka/reader/injectedScrollBridge.js"

private object ReaderBridgeAssets

/** Fallback задержки готовности страницы, если нет `requestAnimationFrame` (~2×16 мс). */
const val READER_READY_RAF_FALLBACK_MS: Int = 32

/** JS, инжектируемый в страницу читалки: throttled scroll, тап-зоны, свайпы. */
fun readerInjectedScrollBridge(): String {
    val stream =
        checkNotNull(ReaderBridgeAssets::class.java.getResourceAsStream(RESOURCE_PATH)) {
            "Missing resource $RESOURCE_PATH"
        }
    return stream.bufferedReader().use { it.readText() }
}

/**
 * Скрипт, выполняемый после `onPageFinished`: начальный скролл и сигнал `ready` после двух rAF.
 */
fun readerLoadEndScrollAndReadyScript(initialScrollY: Double): String {
    val raw = floor(initialScrollY)
    val y =
        if (raw.isFinite()) {
            max(0, raw.toInt())
        } else {
            0
        }
    return """
        (function () {
          try { window.scrollTo(0, $y); } catch (e) {}
          var reportScroll = function () {
            var sy = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;
            var h = document.documentElement.scrollHeight || document.body.scrollHeight || 0;
            var vh = window.innerHeight || document.documentElement.clientHeight || 0;
            var yMax = Math.max(0, h - vh);
            if (window.ReactNativeWebView) {
              window.ReactNativeWebView.postMessage(JSON.stringify({ t: 'scroll', y: sy, yMax: yMax }));
            }
          };
          var ping = function () {
            reportScroll();
            if (window.ReactNativeWebView) {
              window.ReactNativeWebView.postMessage(JSON.stringify({ t: 'ready' }));
            }
          };
          if (typeof requestAnimationFrame === 'function') {
            requestAnimationFrame(function () { requestAnimationFrame(ping); });
          } else {
            setTimeout(ping, $READER_READY_RAF_FALLBACK_MS);
          }
        })();
        true;
    """.trimIndent()
}
