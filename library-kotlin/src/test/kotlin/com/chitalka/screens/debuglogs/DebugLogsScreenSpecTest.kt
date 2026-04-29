package com.chitalka.screens.debuglogs

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugLogsScreenSpecTest {

    @Test
    fun listItemKey() {
        assertEquals("1700000000000-3", DebugLogsScreenSpec.listItemKey(1_700_000_000_000L, 3))
    }

    @Test
    fun toolbarActionsDisabled() {
        assertTrue(DebugLogsScreenSpec.toolbarActionsDisabled(exporting = true, entryCount = 5))
        assertTrue(DebugLogsScreenSpec.toolbarActionsDisabled(exporting = false, entryCount = 0))
        assertFalse(DebugLogsScreenSpec.toolbarActionsDisabled(exporting = false, entryCount = 1))
    }

    @Test
    fun exportFileName_replacesColonsAndDots() {
        val instant = Instant.parse("2026-04-24T12:30:45.123Z")
        val name = DebugLogsScreenSpec.exportFileName(instant)
        assertTrue(name.startsWith("chitalka-logs-"))
        assertTrue(name.endsWith(".txt"))
        val stamp = name.removePrefix("chitalka-logs-").removeSuffix(".txt")
        assertFalse(stamp.contains(":"))
        assertFalse(stamp.contains("."))
    }

    @Test
    fun exportFilePathInCache() {
        assertEquals(
            "/data/cache/chitalka-logs.txt",
            DebugLogsScreenSpec.exportFilePathInCache("/data/cache", "chitalka-logs.txt"),
        )
        assertEquals(
            "/data/cache/chitalka-logs.txt",
            DebugLogsScreenSpec.exportFilePathInCache("/data/cache/", "chitalka-logs.txt"),
        )
    }
}
