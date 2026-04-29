package com.chitalka.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WithTimeoutTest {

    @Test
    fun returns_whenBlockFinishesInTime() = runTest {
        val v = withTimeout(10_000L, "unused") {
            delay(5)
            "done"
        }
        assertEquals("done", v)
    }

    @Test
    fun throws_labeledException_onTimeout() = runTest {
        val err = kotlin.runCatching {
            withTimeout(10L, "TIMEOUT_COPY") {
                delay(10_000)
                "late"
            }
        }.exceptionOrNull()
        assertTrue(err is WithTimeoutException)
        assertEquals("TIMEOUT_COPY", (err as WithTimeoutException).message)
    }
}
