package com.ncorti.kotlin.template.app.ui.reader

import androidx.compose.runtime.Immutable
import com.chitalka.core.types.LibraryBookRecord
import com.chitalka.screens.reader.ReaderScreenSpec

internal enum class ReaderLoadPhase {
    Loading,
    Ready,
    Error,
}

/**
 * Команда переключения слоёв для composable-анимации `Animatable`.
 * VM публикует её, Composable отрабатывает `animateTo` и сообщает результат через колбэк.
 *
 * `revision` инкрементируется на каждый новый запрос — без этого подряд идущие переходы
 * с одинаковыми параметрами не перезапустят `LaunchedEffect`.
 */
@Immutable
internal data class TransitionCommand(
    val revision: Int,
    val targetLayerId: ReaderScreenSpec.ReaderLayerId,
    val direction: Int,
)

/** Иммутабельный снимок состояния читалки. */
@Immutable
internal data class ReaderUiState(
    val phase: ReaderLoadPhase,
    val errorText: String?,
    val spineLength: Int,
    val unpackedRoot: String,
    val layerA: ReaderScreenSpec.ReaderLayerState?,
    val layerB: ReaderScreenSpec.ReaderLayerState?,
    val activeLayerId: ReaderScreenSpec.ReaderLayerId,
    val transitionTargetLayerId: ReaderScreenSpec.ReaderLayerId?,
    val transitionDirection: Int,
    val transitionCommand: TransitionCommand?,
    val busy: Boolean,
    val bookRecord: LibraryBookRecord?,
) {
    fun activeLayer(): ReaderScreenSpec.ReaderLayerState? =
        when (activeLayerId) {
            ReaderScreenSpec.ReaderLayerId.A -> layerA
            ReaderScreenSpec.ReaderLayerId.B -> layerB
        }

    companion object {
        fun initial(): ReaderUiState =
            ReaderUiState(
                phase = ReaderLoadPhase.Loading,
                errorText = null,
                spineLength = 0,
                unpackedRoot = "",
                layerA = null,
                layerB = null,
                activeLayerId = ReaderScreenSpec.ReaderLayerId.A,
                transitionTargetLayerId = null,
                transitionDirection = 1,
                transitionCommand = null,
                busy = false,
                bookRecord = null,
            )
    }
}
