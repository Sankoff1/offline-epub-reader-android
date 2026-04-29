@file:Suppress("LongParameterList", "MagicNumber")

package com.ncorti.kotlin.template.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.chitalka.screens.reader.ReaderScreenSpec
import com.chitalka.screens.reader.incomingPageOpacity
import com.chitalka.screens.reader.incomingPageTranslateXPx
import com.chitalka.screens.reader.incomingShadeOpacity
import com.chitalka.screens.reader.layerHtmlForWebView
import com.chitalka.screens.reader.outgoingPageOpacity
import com.chitalka.screens.reader.outgoingPageTranslateXPx
import com.chitalka.screens.reader.outgoingShadeOpacity
import com.chitalka.theme.ThemeColors
import com.chitalka.theme.ThemeMode
import com.chitalka.ui.readerview.ReaderBridgeInboundMessage
import com.ncorti.kotlin.template.app.ui.theme.parseHexColor

private const val Z_INDEX_BACKGROUND = 0f
private const val Z_INDEX_FOREGROUND = 1f
private const val Z_INDEX_OUTGOING_TOP = 2f

internal fun parseThemeColor(hex: String): Color = parseHexColor(hex)

@Composable
internal fun ReaderPageLayer(
    layerId: ReaderScreenSpec.ReaderLayerId,
    layer: ReaderScreenSpec.ReaderLayerState?,
    activeLayerId: ReaderScreenSpec.ReaderLayerId,
    transitionTargetLayerId: ReaderScreenSpec.ReaderLayerId?,
    isTransitioning: Boolean,
    transitionProgress: Float,
    transitionDirection: Int,
    distancePx: Int,
    readerPaperColor: Color,
    baseUrl: String,
    themeMode: ThemeMode,
    themeColors: ThemeColors,
    onBridge: (ReaderScreenSpec.ReaderLayerId, ReaderBridgeInboundMessage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val l = layer ?: return
    val isActive = layerId == activeLayerId
    val isIncoming = layerId == transitionTargetLayerId
    val isOutgoing = isActive && isTransitioning
    val isIncomingLayer = isIncoming && isTransitioning

    val dir = transitionDirection
    val dist = distancePx

    val (alpha, tx) =
        when {
            isTransitioning && isIncomingLayer ->
                ReaderScreenSpec.incomingPageOpacity(transitionProgress) to
                    ReaderScreenSpec.incomingPageTranslateXPx(transitionProgress, dir, dist)
            isTransitioning && isOutgoing ->
                ReaderScreenSpec.outgoingPageOpacity(transitionProgress) to
                    ReaderScreenSpec.outgoingPageTranslateXPx(transitionProgress, dir, dist)
            isActive && !isTransitioning -> 1f to 0f
            else -> 0f to 0f
        }

    val outgoingShade =
        if (isTransitioning && isOutgoing) {
            ReaderScreenSpec.outgoingShadeOpacity(transitionProgress)
        } else {
            0f
        }
    val incomingShade =
        if (isTransitioning && isIncomingLayer) {
            ReaderScreenSpec.incomingShadeOpacity(transitionProgress)
        } else {
            0f
        }

    val z =
        when {
            isTransitioning && isOutgoing -> Z_INDEX_OUTGOING_TOP
            isTransitioning && isIncomingLayer -> Z_INDEX_FOREGROUND
            isActive -> Z_INDEX_FOREGROUND
            else -> Z_INDEX_BACKGROUND
        }

    val interceptTouches = !(isActive && transitionTargetLayerId == null)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .zIndex(z)
                .graphicsLayer {
                    this.alpha = alpha
                    translationX = tx
                }
                .background(readerPaperColor),
    ) {
        ChitalkaReaderWebView(
            chapterKey = l.token,
            html = ReaderScreenSpec.layerHtmlForWebView(l.html),
            baseUrl = baseUrl,
            initialScrollY = l.initialScrollY,
            themeMode = themeMode,
            themeColors = themeColors,
            interceptAllTouches = interceptTouches,
            onBridgeMessage = { msg -> onBridge(layerId, msg) },
            modifier = Modifier.fillMaxSize(),
        )
        if (incomingShade > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = incomingShade)),
            )
        }
        if (outgoingShade > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = outgoingShade)),
            )
        }
    }
}
