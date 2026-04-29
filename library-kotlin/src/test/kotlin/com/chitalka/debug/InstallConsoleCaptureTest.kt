package com.chitalka.debug

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InstallConsoleCaptureTest {

    @Before
    fun setup() {
        resetConsoleCaptureForTests()
        debugLogClear()
    }

    @After
    fun tearDown() {
        resetConsoleCaptureForTests()
        debugLogClear()
    }

    @Test
    fun stdout_println_goesToDebugLog() {
        installConsoleCapture()
        println("capture-test-line-xyz")
        val snap = debugLogGetSnapshot()
        assertTrue(snap.any { it.message.contains("capture-test-line-xyz") && it.level == DebugLogLevel.Log })
    }

    @Test
    fun second_install_doesNotWrapAgain() {
        installConsoleCapture()
        val first = System.out
        installConsoleCapture()
        assertEquals(first, System.out)
    }
}
