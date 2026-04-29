@file:Suppress("LongParameterList", "LongMethod")

package com.ncorti.kotlin.template.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chitalka.i18n.AppLocale
import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.library.LibrarySessionState
import com.chitalka.navigation.DrawerScreen
import com.chitalka.storage.StorageService
import com.chitalka.theme.ThemeMode
import com.ncorti.kotlin.template.app.ui.debug.ChitalkaDebugLogsPane
import com.ncorti.kotlin.template.app.ui.library.ChitalkaLibraryListPane
import com.ncorti.kotlin.template.app.ui.library.ChitalkaTrashPane
import com.ncorti.kotlin.template.app.ui.library.LibraryListMode
import com.ncorti.kotlin.template.app.ui.library.LibraryViewModel
import com.ncorti.kotlin.template.app.ui.library.TrashViewModel
import com.ncorti.kotlin.template.app.ui.settings.ChitalkaSettingsPane
import com.ncorti.kotlin.template.app.ui.settings.SettingsViewModel

@Composable
internal fun ChitalkaDrawerRouter(
    screen: DrawerScreen,
    controller: ChitalkaAppController,
    librarySession: LibrarySessionState,
    storage: StorageService,
    persistence: LastOpenBookPersistence,
    locale: AppLocale,
    themeMode: ThemeMode,
    listRefreshNonce: Int,
    normalizedSearchQuery: String,
    onRequestImport: () -> Unit,
    onLocaleChange: (AppLocale) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        when (screen) {
            DrawerScreen.ReadingNow ->
                LibraryPane(
                    mode = LibraryListMode.ReadingNow,
                    storage = storage,
                    librarySession = librarySession,
                    controller = controller,
                    listRefreshNonce = listRefreshNonce,
                    normalizedSearchQuery = normalizedSearchQuery,
                    showImportFab = true,
                    onRequestImport = onRequestImport,
                    emptyI18nKey = "screens.readingNow.empty",
                )
            DrawerScreen.BooksAndDocs ->
                LibraryPane(
                    mode = LibraryListMode.All,
                    storage = storage,
                    librarySession = librarySession,
                    controller = controller,
                    listRefreshNonce = listRefreshNonce,
                    normalizedSearchQuery = normalizedSearchQuery,
                    showImportFab = true,
                    onRequestImport = onRequestImport,
                )
            DrawerScreen.Favorites ->
                LibraryPane(
                    mode = LibraryListMode.Favorites,
                    storage = storage,
                    librarySession = librarySession,
                    controller = controller,
                    listRefreshNonce = listRefreshNonce,
                    normalizedSearchQuery = normalizedSearchQuery,
                    showImportFab = false,
                    onRequestImport = onRequestImport,
                )
            DrawerScreen.Cart ->
                TrashPane(
                    storage = storage,
                    librarySession = librarySession,
                    controller = controller,
                    listRefreshNonce = listRefreshNonce,
                    normalizedSearchQuery = normalizedSearchQuery,
                )
            DrawerScreen.DebugLogs ->
                ChitalkaDebugLogsPane()
            DrawerScreen.Settings ->
                SettingsPane(
                    persistence = persistence,
                    locale = locale,
                    themeMode = themeMode,
                    onLocaleChange = onLocaleChange,
                    onThemeModeChange = onThemeModeChange,
                )
        }
    }
}

@Composable
private fun LibraryPane(
    mode: LibraryListMode,
    storage: StorageService,
    librarySession: LibrarySessionState,
    controller: ChitalkaAppController,
    listRefreshNonce: Int,
    normalizedSearchQuery: String,
    showImportFab: Boolean,
    onRequestImport: () -> Unit,
    emptyI18nKey: String = "books.empty",
) {
    val vm: LibraryViewModel = viewModel(
        key = "library:${mode.name}",
        factory = LibraryViewModel.Factory(
            storage = storage,
            librarySession = librarySession,
            mode = mode,
            onLibraryChanged = { controller.bumpLists() },
        ),
    )
    ChitalkaLibraryListPane(
        viewModel = vm,
        listRefreshNonce = listRefreshNonce,
        normalizedSearchQuery = normalizedSearchQuery,
        showImportFab = showImportFab,
        onImportClick = onRequestImport,
        onOpenBook = { b -> controller.openReader(b.record.bookId, b.record.fileUri) },
        emptyI18nKey = emptyI18nKey,
    )
}

@Composable
private fun TrashPane(
    storage: StorageService,
    librarySession: LibrarySessionState,
    controller: ChitalkaAppController,
    listRefreshNonce: Int,
    normalizedSearchQuery: String,
) {
    val vm: TrashViewModel = viewModel(
        key = "trash",
        factory = TrashViewModel.Factory(
            storage = storage,
            librarySession = librarySession,
            onLibraryChanged = { controller.bumpLists() },
        ),
    )
    ChitalkaTrashPane(
        viewModel = vm,
        listRefreshNonce = listRefreshNonce,
        normalizedSearchQuery = normalizedSearchQuery,
    )
}

@Composable
private fun SettingsPane(
    persistence: LastOpenBookPersistence,
    locale: AppLocale,
    themeMode: ThemeMode,
    onLocaleChange: (AppLocale) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val vm: SettingsViewModel = viewModel(
        key = "settings",
        factory = SettingsViewModel.Factory(
            persistence = persistence,
            initialLocale = locale,
            initialThemeMode = themeMode,
            onLocaleChanged = onLocaleChange,
            onThemeModeChanged = onThemeModeChange,
        ),
    )
    ChitalkaSettingsPane(
        viewModel = vm,
        locale = locale,
        themeMode = themeMode,
    )
}
