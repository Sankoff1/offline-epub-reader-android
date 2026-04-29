package com.chitalka.ui.readerview

import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderBridgeScriptsTest {

    @Test
    fun injectedScrollBridge_loadsFromClasspath() {
        val js = readerInjectedScrollBridge()
        assertTrue(js.contains("ReactNativeWebView"))
        assertTrue(js.contains("postY"))
        assertTrue(js.contains("TAP_MAX_DELTA = 10"))
    }

    @Test
    fun loadEndScript_scrollAndReady() {
        val s = readerLoadEndScrollAndReadyScript(42.9)
        assertTrue(s.contains("window.scrollTo(0, 42)"))
        assertTrue(s.contains("'ready'"))
        assertTrue(s.contains("scroll"))
        assertTrue(s.contains("yMax"))
        assertTrue(s.contains("requestAnimationFrame"))
    }

    @Test
    fun loadEndScript_nonFiniteY_becomesZero() {
        assertTrue(readerLoadEndScrollAndReadyScript(Double.NaN).contains("window.scrollTo(0, 0)"))
    }

    @Test
    fun injectedScrollBridge_exposesApplyTheme() {
        val js = readerInjectedScrollBridge()
        assertTrue(js.contains("window.__chitalkaApplyTheme"))
        assertTrue(js.contains("--chitalka-bg"))
        assertTrue(js.contains("--chitalka-fg"))
    }
}
