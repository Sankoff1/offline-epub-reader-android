package com.chitalka.ui.readerview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderBridgeMessagesTest {

    @Test
    fun parseScroll() {
        val m = parseReaderBridgeInboundMessage("""{"t":"scroll","y":12.5}""")
        assertTrue(m is ReaderBridgeInboundMessage.Scroll)
        val scroll = m as ReaderBridgeInboundMessage.Scroll
        assertEquals(12.5, scroll.y, 0.0)
        assertEquals(null, scroll.scrollRangeMax)
    }

    @Test
    fun parseScroll_withYMax() {
        val m = parseReaderBridgeInboundMessage("""{"t":"scroll","y":10,"yMax":800}""")
        assertTrue(m is ReaderBridgeInboundMessage.Scroll)
        val scroll = m as ReaderBridgeInboundMessage.Scroll
        assertEquals(10.0, scroll.y, 0.0)
        assertEquals(800.0, scroll.scrollRangeMax!!, 0.0)
    }

    @Test
    fun parseScroll_rejectsNonFinite() {
        assertNull(parseReaderBridgeInboundMessage("""{"t":"scroll","y":null}"""))
        assertNull(parseReaderBridgeInboundMessage("""{"t":"scroll"}"""))
    }

    @Test
    fun parsePage() {
        val prev = parseReaderBridgeInboundMessage("""{"t":"page","dir":"prev"}""")
        assertEquals(ReaderPageDirection.PREV, (prev as ReaderBridgeInboundMessage.Page).direction)
        val next = parseReaderBridgeInboundMessage("""{"t":"page","dir":"next"}""")
        assertEquals(ReaderPageDirection.NEXT, (next as ReaderBridgeInboundMessage.Page).direction)
        assertNull(parseReaderBridgeInboundMessage("""{"t":"page","dir":"up"}"""))
    }

    @Test
    fun parseReady() {
        assertTrue(parseReaderBridgeInboundMessage("""{"t":"ready"}""") is ReaderBridgeInboundMessage.Ready)
    }

    @Test
    fun parseMalformed_returnsNull() {
        assertNull(parseReaderBridgeInboundMessage("not-json"))
    }

    @Test
    fun encodeReaderBridgeOutboundMessage_applyTheme_format() {
        val json =
            encodeReaderBridgeOutboundMessage(
                ReaderBridgeOutboundMessage.ApplyTheme(
                    backgroundHex = "#121814",
                    foregroundHex = "#E8EFE5",
                    isDark = true,
                ),
            )
        // Стабильный контракт с JS-обработчиком __chitalkaApplyTheme:
        // ключи type/background/foreground/isDark, без лишних полей.
        assertTrue(json.contains("\"type\":\"ApplyTheme\""))
        assertTrue(json.contains("\"background\":\"#121814\""))
        assertTrue(json.contains("\"foreground\":\"#E8EFE5\""))
        assertTrue(json.contains("\"isDark\":true"))
    }

    @Test
    fun encodeReaderBridgeOutboundMessage_applyTheme_lightFlag() {
        val json =
            encodeReaderBridgeOutboundMessage(
                ReaderBridgeOutboundMessage.ApplyTheme(
                    backgroundHex = "#F4F7EE",
                    foregroundHex = "#1F2A1F",
                    isDark = false,
                ),
            )
        assertTrue(json.contains("\"isDark\":false"))
    }
}
