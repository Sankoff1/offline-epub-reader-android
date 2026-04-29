@file:Suppress("MatchingDeclarationName")
package com.chitalka.utils

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout as kotlinxWithTimeout

/**
 * Исключение при срабатывании таймаута; [message] совпадает с переданной меткой.
 */
class WithTimeoutException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Прерывает ожидание через [ms] миллисекунд, если [block] не завершился.
 * Корутины внутри [block] отменяются; блокирующие нативные вызовы могут продолжиться.
 */
suspend fun <T> withTimeout(ms: Long, label: String, block: suspend () -> T): T =
    try {
        kotlinxWithTimeout(ms) { block() }
    } catch (e: TimeoutCancellationException) {
        throw WithTimeoutException(label, e)
    }
