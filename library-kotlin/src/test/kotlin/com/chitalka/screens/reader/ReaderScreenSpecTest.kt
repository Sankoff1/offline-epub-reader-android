package com.chitalka.screens.reader

import com.chitalka.epub.EPUB_EMPTY_SPINE
import com.chitalka.epub.EPUB_ERR_TIMEOUT_COPY
import com.chitalka.ui.readerview.ReaderPageDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderScreenSpecTest {

    @Test
    fun clampChapterIndex() {
        assertEquals(0, ReaderScreenSpec.clampChapterIndex(0, 0))
        assertEquals(2, ReaderScreenSpec.clampChapterIndex(5, 3))
        assertEquals(2, ReaderScreenSpec.clampChapterIndex(99, 3))
        assertEquals(0, ReaderScreenSpec.clampChapterIndex(-3, 5))
    }

    @Test
    fun readerOpenErrorText_epubKnownCodesMappedToKeys() {
        val emptySpine =
            ReaderScreenSpec.readerOpenErrorText(
                ReaderScreenSpec.ReaderOpenErrorKind.Epub(EPUB_EMPTY_SPINE),
            )
        assertEquals(
            ReaderOpenErrorText.Key(ReaderScreenSpec.I18nKeys.ERR_EMPTY_SPINE),
            emptySpine,
        )
        val timeoutCopy =
            ReaderScreenSpec.readerOpenErrorText(
                ReaderScreenSpec.ReaderOpenErrorKind.Epub(EPUB_ERR_TIMEOUT_COPY),
            )
        assertEquals(
            ReaderOpenErrorText.Key(ReaderScreenSpec.I18nKeys.ERR_TIMEOUT_COPY),
            timeoutCopy,
        )
    }

    @Test
    fun readerOpenErrorText_epubCustomMessageReturnedAsLiteral() {
        assertEquals(
            ReaderOpenErrorText.Literal("custom"),
            ReaderScreenSpec.readerOpenErrorText(
                ReaderScreenSpec.ReaderOpenErrorKind.Epub("  custom  "),
            ),
        )
    }

    @Test
    fun readerOpenErrorText_otherBlankFallsBackToUnknownKey() {
        assertEquals(
            ReaderOpenErrorText.Key(ReaderScreenSpec.I18nKeys.ERR_UNKNOWN),
            ReaderScreenSpec.readerOpenErrorText(
                ReaderScreenSpec.ReaderOpenErrorKind.Other("  "),
            ),
        )
    }

    @Test
    fun readerOpenErrorText_otherWithMessageReturnedAsLiteral() {
        assertEquals(
            ReaderOpenErrorText.Literal("Boom"),
            ReaderScreenSpec.readerOpenErrorText(
                ReaderScreenSpec.ReaderOpenErrorKind.Other("Boom"),
            ),
        )
    }

    @Test
    fun inactiveLayerId() {
        assertEquals(
            ReaderScreenSpec.ReaderLayerId.B,
            ReaderScreenSpec.inactiveLayerId(ReaderScreenSpec.ReaderLayerId.A),
        )
        assertEquals(
            ReaderScreenSpec.ReaderLayerId.A,
            ReaderScreenSpec.inactiveLayerId(ReaderScreenSpec.ReaderLayerId.B),
        )
    }

    @Test
    fun pageIndicatorSlash() {
        assertEquals("3/10", ReaderScreenSpec.pageIndicatorSlash(2, 10))
    }

    @Test
    fun normalizeSavedScrollOffset() {
        assertEquals(0.0, ReaderScreenSpec.normalizeSavedScrollOffset(null), 0.0)
        assertEquals(0.0, ReaderScreenSpec.normalizeSavedScrollOffset(Double.NaN), 0.0)
        assertEquals(12.5, ReaderScreenSpec.normalizeSavedScrollOffset(12.5), 0.0)
    }

    @Test
    fun normalizeSavedScrollRangeMax() {
        assertEquals(0.0, ReaderScreenSpec.normalizeSavedScrollRangeMax(null), 0.0)
        assertEquals(0.0, ReaderScreenSpec.normalizeSavedScrollRangeMax(Double.NaN), 0.0)
        assertEquals(0.0, ReaderScreenSpec.normalizeSavedScrollRangeMax(-1.0), 0.0)
        assertEquals(400.0, ReaderScreenSpec.normalizeSavedScrollRangeMax(400.0), 0.0)
    }

    @Test
    fun layerHtmlForWebView_emptyUsesConstant() {
        assertEquals(
            ReaderScreenSpec.EMPTY_READER_HTML,
            ReaderScreenSpec.layerHtmlForWebView("   \n"),
        )
    }

    @Test
    fun webViewBaseUrl_trailingSlash() {
        assertEquals("file:///x/", ReaderScreenSpec.webViewBaseUrl("file:///x"))
        assertEquals("file:///x/", ReaderScreenSpec.webViewBaseUrl("file:///x/"))
    }

    @Test
    fun shouldWarnUnpackedOutsideDocuments() {
        assertFalse(
            ReaderScreenSpec.shouldWarnUnpackedOutsideDocuments(
                "/data/user/0/app/files/book_cache/1/",
                "/data/user/0/app/files/",
            ),
        )
        assertTrue(
            ReaderScreenSpec.shouldWarnUnpackedOutsideDocuments(
                "/tmp/out/",
                "/data/user/0/app/files/",
            ),
        )
    }

    @Test
    fun layerToken() {
        assertEquals("b-2-99", ReaderScreenSpec.layerToken("b", 2, 99))
    }

    @Test
    fun transitionDirectionSign() {
        assertEquals(1, ReaderScreenSpec.transitionDirectionSign(3, 1))
        assertEquals(-1, ReaderScreenSpec.transitionDirectionSign(1, 3))
    }

    @Test
    fun targetChapterForPageTurn() {
        assertEquals(
            1,
            ReaderScreenSpec.targetChapterForPageTurn(0, 5, ReaderPageDirection.NEXT),
        )
        assertEquals(
            0,
            ReaderScreenSpec.targetChapterForPageTurn(0, 5, ReaderPageDirection.PREV),
        )
        assertEquals(
            4,
            ReaderScreenSpec.targetChapterForPageTurn(4, 5, ReaderPageDirection.NEXT),
        )
    }

    @Test
    fun canAttemptChapterChange() {
        assertFalse(
            ReaderScreenSpec.canAttemptChapterChange(
                epubNonNull = true,
                spineLength = 0,
                phaseReady = true,
                flipping = false,
                currentLayerNonNull = true,
            ),
        )
        assertTrue(
            ReaderScreenSpec.canAttemptChapterChange(
                epubNonNull = true,
                spineLength = 3,
                phaseReady = true,
                flipping = false,
                currentLayerNonNull = true,
            ),
        )
    }

    @Test
    fun outgoingPageOpacity_keyframes() {
        assertEquals(1f, ReaderScreenSpec.outgoingPageOpacity(0f), 1e-4f)
        assertEquals(0.25f, ReaderScreenSpec.outgoingPageOpacity(0.6f), 1e-4f)
        assertEquals(0f, ReaderScreenSpec.outgoingPageOpacity(1f), 1e-4f)
    }

    @Test
    fun incomingPageOpacity_keyframes() {
        assertEquals(0f, ReaderScreenSpec.incomingPageOpacity(0f), 1e-4f)
        assertEquals(0f, ReaderScreenSpec.incomingPageOpacity(0.29f), 1e-4f)
        assertEquals(1f, ReaderScreenSpec.incomingPageOpacity(1f), 1e-4f)
    }

    @Test
    fun incomingShadeOpacity_peakAtPointThree() {
        val at = ReaderScreenSpec.incomingShadeOpacity(0.3f)
        assertEquals(0.06f, at, 1e-4f)
        assertEquals(0f, ReaderScreenSpec.incomingShadeOpacity(1f), 1e-4f)
    }

    @Test
    fun translatePx_endpoints() {
        val d = 400
        assertEquals(
            0f,
            ReaderScreenSpec.outgoingPageTranslateXPx(0f, 1, d),
            0.01f,
        )
        assertEquals(
            -0.12f * d,
            ReaderScreenSpec.outgoingPageTranslateXPx(1f, 1, d),
            0.01f,
        )
        assertEquals(
            0.12f * d,
            ReaderScreenSpec.incomingPageTranslateXPx(0f, 1, d),
            0.01f,
        )
    }

}
