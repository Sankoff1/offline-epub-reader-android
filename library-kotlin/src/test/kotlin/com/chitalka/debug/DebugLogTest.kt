package com.chitalka.debug

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DebugLogTest {

    @Before
    fun clear() {
        debugLogClear()
    }

    @Test
    fun maxEntries_keepsLast4000() {
        repeat(4001) { i ->
            debugLogAppend(DebugLogLevel.Log, "m$i")
        }
        val snap = debugLogGetSnapshot()
        assertEquals(4000, snap.size)
        assertEquals("m1", snap.first().message)
        assertEquals("m4000", snap.last().message)
    }

    @Test
    fun getSnapshot_isCopy() {
        debugLogAppend(DebugLogLevel.Warn, "a")
        val a = debugLogGetSnapshot()
        debugLogClear()
        assertEquals(1, a.size)
        assertTrue(debugLogGetSnapshot().isEmpty())
    }

    @Test
    fun subscribe_andUnsubscribe() {
        var n = 0
        val unsub = debugLogSubscribe { n++ }
        debugLogAppend(DebugLogLevel.Error, "x")
        assertEquals(1, n)
        unsub()
        debugLogAppend(DebugLogLevel.Error, "y")
        assertEquals(1, n)
    }

    @Test
    fun formatExport_containsHeaderAndLevels() {
        debugLogAppend(DebugLogLevel.Info, "hello")
        val text = debugLogFormatExport()
        assertTrue(text.contains("Chitalka debug log export"))
        assertTrue(text.contains("Entries: 1"))
        assertTrue(text.contains("[info]"))
        assertTrue(text.contains("hello"))
    }

    @Test
    fun wireNames_matchTypeScript() {
        assertEquals("log", DebugLogLevel.Log.wireName)
        assertEquals("warn", DebugLogLevel.Warn.wireName)
        assertEquals("error", DebugLogLevel.Error.wireName)
        assertEquals("debug", DebugLogLevel.Debug.wireName)
        assertEquals("info", DebugLogLevel.Info.wireName)
    }

    @Test
    fun getSnapshot_returnsNewListEachCall() {
        debugLogAppend(DebugLogLevel.Log, "z")
        val s1 = debugLogGetSnapshot()
        val s2 = debugLogGetSnapshot()
        assertEquals(s1, s2)
        assertNotSame(s1, s2)
    }
}
