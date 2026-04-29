package com.chitalka.navigation

import com.chitalka.debug.ChitalkaMirrorLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val LOG_TAG = "Chitalka"
private const val RETRY_MS = 50L
private const val MAX_ATTEMPTS = 50

/**
 * Координатор перехода в читалку: откладывает навигацию до готовности `NavHost`.
 *
 * Передайте `isNavHostReady` (например `navController.currentDestination != null`)
 * и `performNavigateToReader` — фактический вызов `navController.navigate(...)`.
 *
 * [scope] должен использовать main-dispatcher, чтобы обращения к NavController шли с UI-потока.
 */
class ReaderNavCoordinator(
    private val scope: CoroutineScope,
    private val isNavHostReady: () -> Boolean,
    private val performNavigateToReader: (ReaderRouteParams) -> Unit,
) {
    private val lock = Any()
    private var pendingReader: ReaderRouteParams? = null
    private var retryJob: Job? = null

    /** Вызывать при появлении/смене активного destination, чтобы не потерять отложенный переход. */
    fun flushReaderNavigationIfPending() {
        synchronized(lock) {
            val pending = pendingReader ?: return
            if (!isNavHostReady()) {
                return
            }
            pendingReader = null
            performNavigateToReader(pending)
        }
    }

    /** Запланировать переход в читалку. Если NavHost не готов — повторяем 50 мс × 50. */
    fun navigateToReader(bookPath: String, bookId: String) {
        synchronized(lock) {
            pendingReader = ReaderRouteParams(bookPath = bookPath, bookId = bookId)
        }
        flushReaderNavigationIfPending()
        synchronized(lock) {
            if (pendingReader == null) {
                return
            }
        }
        retryJob?.cancel()
        retryJob =
            scope.launch {
                repeat(MAX_ATTEMPTS) {
                    delay(RETRY_MS)
                    flushReaderNavigationIfPending()
                    synchronized(lock) {
                        if (pendingReader == null) {
                            return@launch
                        }
                    }
                }
                ChitalkaMirrorLog.w(LOG_TAG, "[Chitalka] navigateToReader: navigation не стал ready за отведённое время")
                synchronized(lock) {
                    pendingReader = null
                }
            }
    }

    /** Сбросить отложенный переход (например, при destroy Activity). */
    fun clearPendingReader() {
        retryJob?.cancel()
        retryJob = null
        synchronized(lock) {
            pendingReader = null
        }
    }
}
