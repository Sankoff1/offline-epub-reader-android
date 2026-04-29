package com.ncorti.kotlin.template.app.ui.i18n

import com.ncorti.kotlin.template.app.R

/**
 * Явный маппинг i18n-ключей (как они описаны в `*ScreenSpec.I18nKeys` в `library-kotlin`)
 * на `R.string.*` идентификаторы. Через `Resources.getIdentifier` ходить нельзя — R8/AAPT2
 * shrink дропнет «неиспользуемые» строки.
 */
object ChitalkaStringIds {

    private val map: Map<String, Int> =
        mapOf(
            // drawer
            "drawer.readingNow" to R.string.drawer_readingNow,
            "drawer.books" to R.string.drawer_books,
            "drawer.favorites" to R.string.drawer_favorites,
            "drawer.cart" to R.string.drawer_cart,
            "drawer.debugLogs" to R.string.drawer_debugLogs,
            "drawer.settings" to R.string.drawer_settings,

            // screens
            "screens.readingNow.title" to R.string.screens_readingNow_title,
            "screens.readingNow.subtitle" to R.string.screens_readingNow_subtitle,
            "screens.readingNow.empty" to R.string.screens_readingNow_empty,
            "screens.favorites.title" to R.string.screens_favorites_title,
            "screens.favorites.subtitle" to R.string.screens_favorites_subtitle,
            "screens.favorites.empty" to R.string.screens_favorites_empty,
            "screens.cart.title" to R.string.screens_cart_title,
            "screens.cart.subtitle" to R.string.screens_cart_subtitle,
            "screens.cart.empty" to R.string.screens_cart_empty,

            // debug logs
            "debugLogs.title" to R.string.debugLogs_title,
            "debugLogs.subtitle" to R.string.debugLogs_subtitle,
            "debugLogs.clear" to R.string.debugLogs_clear,
            "debugLogs.copy" to R.string.debugLogs_copy,
            "debugLogs.export" to R.string.debugLogs_export,
            "debugLogs.empty" to R.string.debugLogs_empty,
            "debugLogs.exportFailed" to R.string.debugLogs_exportFailed,
            "debugLogs.exportNoCache" to R.string.debugLogs_exportNoCache,
            "debugLogs.exportSaved" to R.string.debugLogs_exportSaved,
            "debugLogs.exportDialogTitle" to R.string.debugLogs_exportDialogTitle,

            // settings
            "settings.title" to R.string.settings_title,
            "settings.themeSection" to R.string.settings_themeSection,
            "settings.themeLight" to R.string.settings_themeLight,
            "settings.themeDark" to R.string.settings_themeDark,
            "settings.darkTheme" to R.string.settings_darkTheme,
            "settings.languageSection" to R.string.settings_languageSection,
            "settings.languageRu" to R.string.settings_languageRu,
            "settings.languageEn" to R.string.settings_languageEn,
            "settings.versionLabel" to R.string.settings_versionLabel,

            // first launch
            "firstLaunch.message" to R.string.firstLaunch_message,
            "firstLaunch.cancel" to R.string.firstLaunch_cancel,
            "firstLaunch.pickEpub" to R.string.firstLaunch_pickEpub,

            // books
            "books.empty" to R.string.books_empty,
            "books.addBookA11y" to R.string.books_addBookA11y,
            "books.readPercent" to R.string.books_readPercent,

            // reader
            "reader.backToLibrary" to R.string.reader_backToLibrary,
            "reader.back" to R.string.reader_back,
            "reader.forward" to R.string.reader_forward,
            "reader.chapterProgress" to R.string.reader_chapterProgress,
            "reader.chapterLabel" to R.string.reader_chapterLabel,
            "reader.toc" to R.string.reader_toc,
            "reader.loading" to R.string.reader_loading,
            "reader.errorTitle" to R.string.reader_errorTitle,
            "reader.backToBooks" to R.string.reader_backToBooks,
            "reader.errors.emptySpine" to R.string.reader_errors_emptySpine,
            "reader.errors.openFailed" to R.string.reader_errors_openFailed,
            "reader.errors.unknown" to R.string.reader_errors_unknown,
            "reader.errors.timeoutCopy" to R.string.reader_errors_timeoutCopy,
            "reader.errors.timeoutUnzip" to R.string.reader_errors_timeoutUnzip,
            "reader.errors.timeoutPrepareChapter" to R.string.reader_errors_timeoutPrepareChapter,
            "reader.errors.copyFailed" to R.string.reader_errors_copyFailed,
            "reader.errors.unzipFailed" to R.string.reader_errors_unzipFailed,
            "reader.errors.containerMissing" to R.string.reader_errors_containerMissing,
            "reader.errors.opfParseFailed" to R.string.reader_errors_opfParseFailed,
            "reader.errors.chapterReadFailed" to R.string.reader_errors_chapterReadFailed,
            "reader.errors.badSourceUri" to R.string.reader_errors_badSourceUri,
            "reader.errors.internal" to R.string.reader_errors_internal,

            // library
            "library.importFailed" to R.string.library_importFailed,

            // picker
            "picker.invalidExtension" to R.string.picker_invalidExtension,
            "picker.openFailed" to R.string.picker_openFailed,

            // book
            "book.untitled" to R.string.book_untitled,
            "book.unknownAuthor" to R.string.book_unknownAuthor,

            // common
            "common.mb" to R.string.common_mb,
            "common.cancel" to R.string.common_cancel,

            // library screen
            "libraryScreen.title" to R.string.libraryScreen_title,
            "libraryScreen.subtitle" to R.string.libraryScreen_subtitle,
            "libraryScreen.pickEpub" to R.string.libraryScreen_pickEpub,

            // a11y
            "a11y.openMenu" to R.string.a11y_openMenu,
            "a11y.addBook" to R.string.a11y_addBook,
            "a11y.search" to R.string.a11y_search,
            "a11y.closeSearch" to R.string.a11y_closeSearch,
            "a11y.languagePicker" to R.string.a11y_languagePicker,
            "a11y.dismissOverlay" to R.string.a11y_dismissOverlay,

            // search
            "search.placeholder" to R.string.search_placeholder,
            "search.noResults" to R.string.search_noResults,

            // book actions
            "bookActions.title" to R.string.bookActions_title,
            "bookActions.addToFavorites" to R.string.bookActions_addToFavorites,
            "bookActions.removeFromFavorites" to R.string.bookActions_removeFromFavorites,
            "bookActions.moveToTrash" to R.string.bookActions_moveToTrash,

            // trash
            "trash.restore" to R.string.trash_restore,
            "trash.deleteForever" to R.string.trash_deleteForever,
            "trash.confirmDeleteTitle" to R.string.trash_confirmDeleteTitle,
            "trash.confirmDeleteMessage" to R.string.trash_confirmDeleteMessage,
            "trash.deleteFailed" to R.string.trash_deleteFailed,

            // storage errors
            "storage.errors.openFailed" to R.string.storage_errors_openFailed,
            "storage.errors.generic" to R.string.storage_errors_generic,
            "storage.errors.invalidBookId" to R.string.storage_errors_invalidBookId,
            "storage.errors.invalidProgressOffset" to R.string.storage_errors_invalidProgressOffset,
            "storage.errors.invalidProgressScrollRange" to R.string.storage_errors_invalidProgressScrollRange,
        )

    fun idFor(key: String): Int =
        map[key] ?: error("Unknown chitalka i18n key: $key")
}
