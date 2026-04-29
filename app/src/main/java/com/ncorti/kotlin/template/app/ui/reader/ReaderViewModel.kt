@file:Suppress("LongParameterList", "TooGenericExceptionCaught")

package com.ncorti.kotlin.template.app.ui.reader

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chitalka.core.types.ReadingProgress
import com.chitalka.debug.ChitalkaMirrorLog
import com.chitalka.epub.EPUB_EMPTY_SPINE
import com.chitalka.epub.EpubService
import com.chitalka.epub.EpubServiceError
import com.chitalka.i18n.AppLocale
import com.chitalka.library.LibrarySessionState
import com.chitalka.library.refreshBookCount
import com.chitalka.screens.reader.ReaderOpenErrorText
import com.chitalka.screens.reader.ReaderScreenSpec
import com.chitalka.screens.reader.clampChapterIndex
import com.chitalka.screens.reader.layerToken
import com.chitalka.screens.reader.normalizeSavedScrollOffset
import com.chitalka.screens.reader.readerOpenErrorText
import com.chitalka.storage.StorageService
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal const val READER_LOG_TAG = "Reader"

internal class ReaderViewModel(
    val bookId: String,
    private val storage: StorageService,
    private val librarySession: LibrarySessionState,
    private val locale: AppLocale,
    private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(ReaderUiState.initial())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    internal var epub: EpubService? = null

    /** Последняя позиция скролла активного слоя — не часть UI-state, нужна только для сохранения. */
    @Volatile internal var latestScroll: Double = 0.0
    @Volatile internal var latestScrollRangeMax: Double = 0.0

    internal var scrollBridgeJob: Job? = null
    internal var persistJob: Job? = null
    internal val incomingGateRef = AtomicReference<CompletableDeferred<Unit>?>(null)

    private var transitionRevision: Int = 0

    fun onBookRecordRefresh() {
        viewModelScope.launch {
            val record = storage.getLibraryBook(bookId)
            _state.update { it.copy(bookRecord = record) }
        }
    }

    fun initialize(context: Context, nativePath: String) {
        viewModelScope.launch { initializeInternal(context, nativePath) }
    }

    fun goToChapter(targetIndex: Int) {
        viewModelScope.launch { goToChapterInternal(targetIndex) }
    }

    fun handleBridge(
        layerId: ReaderScreenSpec.ReaderLayerId,
        msg: com.chitalka.ui.readerview.ReaderBridgeInboundMessage,
    ) {
        handleBridgeInternal(layerId, msg)
    }

    /** Вызывается из Composable после завершения `Animatable.animateTo`. */
    fun onTransitionAnimationFinished() {
        val s = _state.value
        val target = s.transitionTargetLayerId ?: return
        latestScroll = 0.0
        latestScrollRangeMax = 0.0
        _state.update {
            it.copy(
                activeLayerId = target,
                transitionTargetLayerId = null,
                transitionCommand = null,
                busy = false,
            )
        }
    }

    internal fun nextTransitionRevision(): Int = ++transitionRevision

    internal fun mutate(reducer: (ReaderUiState) -> ReaderUiState) {
        _state.update(reducer)
    }

    internal fun currentState(): ReaderUiState = _state.value

    override fun onCleared() {
        val s = _state.value
        if (s.phase == ReaderLoadPhase.Ready) {
            s.activeLayer()?.let { layer ->
                try {
                    // Финальный сброс прогресса синхронно — viewModelScope уже отменён.
                    runBlocking {
                        saveProgressSafely(layer.chapterIndex, latestScroll, latestScrollRangeMax)
                    }
                } catch (e: Exception) {
                    ChitalkaMirrorLog.w(READER_LOG_TAG, "dispose flush progress failed bookId=$bookId", e)
                }
            }
        }
        epub?.destroy()
        epub = null
        super.onCleared()
    }

    private suspend fun initializeInternal(context: Context, nativePath: String) {
        _state.update {
            it.copy(
                phase = ReaderLoadPhase.Loading,
                errorText = null,
                layerA = null,
                layerB = null,
                activeLayerId = ReaderScreenSpec.ReaderLayerId.A,
                transitionTargetLayerId = null,
                transitionCommand = null,
                busy = false,
            )
        }
        incomingGateRef.set(null)
        epub?.destroy()
        val service = EpubService(context, nativePath)
        epub = service
        try {
            val progress = storage.getProgress(bookId)
            val structure = service.open()
            if (structure.spine.isEmpty()) {
                throw EpubServiceError(EPUB_EMPTY_SPINE)
            }
            val spineLength = structure.spine.size
            runCatching { storage.setBookTotalChapters(bookId, spineLength) }
                .onFailure { ChitalkaMirrorLog.w(READER_LOG_TAG, "setBookTotalChapters failed bookId=$bookId", it) }
            val savedIndex =
                progress?.let { ReaderScreenSpec.clampChapterIndex(it.lastChapterIndex, spineLength) }
                    ?: 0
            val scroll = ReaderScreenSpec.normalizeSavedScrollOffset(progress?.scrollOffset)
            val scrollMax = ReaderScreenSpec.normalizeSavedScrollRangeMax(progress?.scrollRangeMax)
            val uri = service.getSpineChapterUri(savedIndex)
            val html = service.prepareChapter(uri)
            latestScroll = scroll
            latestScrollRangeMax = scrollMax
            val initialLayer =
                ReaderScreenSpec.ReaderLayerState(
                    chapterIndex = savedIndex,
                    html = html,
                    initialScrollY = scroll,
                    token = ReaderScreenSpec.layerToken(bookId, savedIndex, System.currentTimeMillis()),
                )
            _state.update {
                it.copy(
                    phase = ReaderLoadPhase.Ready,
                    spineLength = spineLength,
                    unpackedRoot = structure.unpackedRootUri,
                    layerA = initialLayer,
                    layerB = null,
                    activeLayerId = ReaderScreenSpec.ReaderLayerId.A,
                    transitionTargetLayerId = null,
                    transitionCommand = null,
                )
            }
            flushInitialProgress(savedIndex, scroll, scrollMax)
        } catch (e: Exception) {
            service.destroy()
            epub = null
            _state.update {
                it.copy(
                    phase = ReaderLoadPhase.Error,
                    errorText = openErrorText(e),
                )
            }
        }
    }

    internal fun openErrorText(e: Exception): String {
        val kind =
            when (e) {
                is EpubServiceError ->
                    ReaderScreenSpec.ReaderOpenErrorKind.Epub(e.message ?: "")
                else ->
                    ReaderScreenSpec.ReaderOpenErrorKind.Other(e.message)
            }
        return when (val res = ReaderScreenSpec.readerOpenErrorText(kind)) {
            is ReaderOpenErrorText.Key -> chitalkaString(appContext, locale, res.i18nKey)
            is ReaderOpenErrorText.Literal -> res.text
        }
    }

    internal fun schedulePersist(index: Int, scrollY: Double) {
        persistJob?.cancel()
        persistJob =
            viewModelScope.launch {
                delay(ReaderScreenSpec.Timing.SCROLL_PERSIST_DEBOUNCE_MS)
                saveProgressSafely(index, scrollY, latestScrollRangeMax)
            }
    }

    internal suspend fun saveProgressSafely(
        index: Int,
        scrollY: Double,
        scrollRangeMax: Double,
    ) {
        try {
            storage.saveProgress(
                ReadingProgress(
                    bookId = bookId,
                    lastChapterIndex = index,
                    scrollOffset = scrollY,
                    scrollRangeMax = scrollRangeMax,
                    lastReadTimestamp = System.currentTimeMillis(),
                ),
            )
        } catch (e: Exception) {
            ChitalkaMirrorLog.w(READER_LOG_TAG, "saveProgress failed bookId=$bookId chapter=$index", e)
        }
    }

    private suspend fun flushInitialProgress(
        chapterIndex: Int,
        scrollY: Double,
        scrollRangeMax: Double,
    ) {
        try {
            storage.saveProgress(
                ReadingProgress(
                    bookId = bookId,
                    lastChapterIndex = chapterIndex,
                    scrollOffset = scrollY,
                    scrollRangeMax = scrollRangeMax,
                    lastReadTimestamp = System.currentTimeMillis(),
                ),
            )
            librarySession.refreshBookCount(storage)
        } catch (e: Exception) {
            ChitalkaMirrorLog.w(READER_LOG_TAG, "initial saveProgress / refresh failed bookId=$bookId", e)
        }
    }

    internal fun launchInVmScope(block: suspend () -> Unit): Job =
        viewModelScope.launch { block() }

    class Factory(
        private val bookId: String,
        private val storage: StorageService,
        private val librarySession: LibrarySessionState,
        private val locale: AppLocale,
        private val appContext: Context,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ReaderViewModel::class.java)) {
                "Unknown ViewModel class: $modelClass"
            }
            return ReaderViewModel(bookId, storage, librarySession, locale, appContext) as T
        }
    }
}
