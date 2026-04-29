package com.chitalka.library

/** Контракт UI-строк, связанных с импортом книги в библиотеку. */
object LibraryImportSpec {

    object I18nKeys {
        /** Заголовок-фолбэк, когда EPUB не содержит метаданных. */
        const val BOOK_UNTITLED = "book.untitled"

        /** Автор-фолбэк, когда EPUB не содержит метаданных. */
        const val BOOK_UNKNOWN_AUTHOR = "book.unknownAuthor"

        /** Сообщение об ошибке импорта, показываемое в welcome-модалке. */
        const val IMPORT_FAILED = "library.importFailed"

        /** Подпись доступности FAB добавления книги. */
        const val ADD_BOOK_A11Y = "books.addBookA11y"
    }
}
