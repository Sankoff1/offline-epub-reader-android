package com.chitalka.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StorageErrorMessagesTest {

    @Test
    fun knownCodes_returnI18nKeys() {
        assertEquals("storage.errors.openFailed", storageErrorKey(STORAGE_ERR_OPEN_FAILED))
        assertEquals("storage.errors.invalidBookId", storageErrorKey(STORAGE_ERR_INVALID_BOOK_ID))
        assertEquals("storage.errors.generic", storageErrorKey(STORAGE_ERR_GENERIC))
        assertEquals("storage.errors.invalidProgressOffset", storageErrorKey(STORAGE_ERR_INVALID_PROGRESS_OFFSET))
        assertEquals(
            "storage.errors.invalidProgressScrollRange",
            storageErrorKey(STORAGE_ERR_INVALID_PROGRESS_SCROLL_RANGE),
        )
    }

    @Test
    fun unknownCode_returnsNull() {
        assertNull(storageErrorKey("FOO_BAR"))
    }
}
