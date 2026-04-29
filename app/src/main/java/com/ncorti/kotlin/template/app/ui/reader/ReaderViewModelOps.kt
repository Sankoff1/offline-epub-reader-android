@file:Suppress(
    "MagicNumber",
    "TooGenericExceptionCaught",
    "ReturnCount",
    "LongMethod",
    "CyclomaticComplexMethod",
)

package com.ncorti.kotlin.template.app.ui.reader

import com.chitalka.screens.reader.ReaderScreenSpec
import com.chitalka.screens.reader.canAttemptChapterChange
import com.chitalka.screens.reader.clampChapterIndex
import com.chitalka.screens.reader.inactiveLayerId
import com.chitalka.screens.reader.layerToken
import com.chitalka.screens.reader.shouldSkipChapterNavigation
import com.chitalka.screens.reader.transitionDirectionSign
import com.chitalka.ui.readerview.READER_BRIDGE_SCROLL_DEBOUNCE_MS
import com.chitalka.ui.readerview.ReaderBridgeInboundMessage
import com.chitalka.ui.readerview.ReaderPageDirection
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

internal suspend fun ReaderViewModel.goToChapterInternal(targetIndex: Int) {
    val svc = epub ?: return
    val s = currentState()
    val currentLayer = s.activeLayer()
    if (
        !ReaderScreenSpec.canAttemptChapterChange(
            epubNonNull = true,
            spineLength = s.spineLength,
            phaseReady = s.phase == ReaderLoadPhase.Ready,
            flipping = s.busy,
            currentLayerNonNull = currentLayer != null,
        )
    ) {
        return
    }
    val clamped = ReaderScreenSpec.clampChapterIndex(targetIndex, s.spineLength)
    if (ReaderScreenSpec.shouldSkipChapterNavigation(clamped, currentLayer!!.chapterIndex)) {
        return
    }

    mutate { it.copy(busy = true) }
    try {
        saveProgressSafely(currentLayer.chapterIndex, latestScroll, latestScrollRangeMax)
        val html = svc.prepareChapter(svc.getSpineChapterUri(clamped))
        val targetLayerId = ReaderScreenSpec.inactiveLayerId(s.activeLayerId)
        val direction = ReaderScreenSpec.transitionDirectionSign(clamped, currentLayer.chapterIndex)
        val nextLayer =
            ReaderScreenSpec.ReaderLayerState(
                chapterIndex = clamped,
                html = html,
                initialScrollY = 0.0,
                token = ReaderScreenSpec.layerToken(bookId, clamped, System.currentTimeMillis()),
            )

        val gate = CompletableDeferred<Unit>()
        incomingGateRef.set(gate)

        mutate { st ->
            val newA = if (targetLayerId == ReaderScreenSpec.ReaderLayerId.A) nextLayer else st.layerA
            val newB = if (targetLayerId == ReaderScreenSpec.ReaderLayerId.B) nextLayer else st.layerB
            st.copy(
                layerA = newA,
                layerB = newB,
                transitionTargetLayerId = targetLayerId,
                transitionDirection = direction,
                transitionCommand =
                    TransitionCommand(
                        revision = nextTransitionRevision(),
                        targetLayerId = targetLayerId,
                        direction = direction,
                    ),
            )
        }

        withTimeoutOrNull(ReaderScreenSpec.Timing.PENDING_LAYER_READY_TIMEOUT_MS) {
            gate.await()
        }
        incomingGateRef.set(null)

        // Анимация выполняется в Composable. Сброс `busy`, активного слоя и постсохранение
        // прогресса делает onTransitionAnimationFinished, чтобы повторные goToChapter
        // не запускались до окончания текущего перехода.
        saveProgressSafely(clamped, 0.0, 0.0)
    } catch (e: Exception) {
        incomingGateRef.set(null)
        mutate {
            it.copy(
                transitionTargetLayerId = null,
                transitionCommand = null,
                busy = false,
                phase = ReaderLoadPhase.Error,
                errorText = openErrorText(e),
            )
        }
    }
}

internal fun ReaderViewModel.handleBridgeInternal(
    layerId: ReaderScreenSpec.ReaderLayerId,
    msg: ReaderBridgeInboundMessage,
) {
    when (msg) {
        ReaderBridgeInboundMessage.Ready -> {
            val s = currentState()
            if (layerId == s.transitionTargetLayerId) {
                incomingGateRef.get()?.let { gate ->
                    if (!gate.isCompleted) {
                        gate.complete(Unit)
                    }
                }
            }
        }
        is ReaderBridgeInboundMessage.Scroll -> {
            val s = currentState()
            val transitioning = s.transitionTargetLayerId != null
            val isActive = layerId == s.activeLayerId
            if (isActive && !transitioning) {
                scrollBridgeJob?.cancel()
                scrollBridgeJob =
                    launchInVmScope {
                        delay(READER_BRIDGE_SCROLL_DEBOUNCE_MS)
                        latestScroll = msg.y
                        msg.scrollRangeMax?.let { yMax ->
                            if (yMax.isFinite() && yMax >= 0.0) {
                                latestScrollRangeMax = yMax
                            }
                        }
                        currentState().activeLayer()?.let { layer ->
                            schedulePersist(layer.chapterIndex, msg.y)
                        }
                    }
            }
        }
        is ReaderBridgeInboundMessage.Page -> {
            val s = currentState()
            val transitioning = s.transitionTargetLayerId != null
            val isActive = layerId == s.activeLayerId
            if (isActive && !transitioning && !s.busy) {
                val delta =
                    when (msg.direction) {
                        ReaderPageDirection.NEXT -> 1
                        ReaderPageDirection.PREV -> -1
                    }
                val cur = s.activeLayer()?.chapterIndex ?: return
                goToChapter(cur + delta)
            }
        }
    }
}
