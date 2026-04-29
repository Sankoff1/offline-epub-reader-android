package com.chitalka.navigation

import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.library.clearLastOpenBookId
import com.chitalka.library.setLastOpenBookId

/**
 * Lifecycle-крючки маршрута читалки: ключ «последняя книга» + обновление счётчика библиотеки.
 *
 * [onReaderStackBeforeRemove] вызывать только при реальном уходе с маршрута (back / popBackStack);
 * из `onDispose`/пересборки Compose — нельзя, иначе ключ потеряется и автооткрытие не сработает.
 */
object ReaderRouteLifecycle {
    /** При входе на маршрут читалки запоминаем id для автооткрытия на следующем запуске. */
    suspend fun onReaderEntered(
        persistence: LastOpenBookPersistence,
        bookId: String,
    ) {
        setLastOpenBookId(persistence, bookId)
    }

    /** Перед снятием экрана читалки со стека — очистить ключ «последняя книга». */
    suspend fun onReaderStackBeforeRemove(persistence: LastOpenBookPersistence) {
        clearLastOpenBookId(persistence)
    }

    /** Кнопка «в библиотеку»: обновить счётчик книг и выполнить возврат. */
    suspend fun onBackToLibrary(
        refreshBookCount: suspend () -> Unit,
        goBack: () -> Unit,
    ) {
        refreshBookCount()
        goBack()
    }

    /** После того как читалка открыла книгу — обновить счётчик. */
    suspend fun onReaderContentOpened(refreshBookCount: suspend () -> Unit) {
        refreshBookCount()
    }
}
