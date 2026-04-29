@file:Suppress("LongMethod")

package com.ncorti.kotlin.template.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import com.chitalka.i18n.AppLocale
import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.library.LibrarySessionState
import com.chitalka.library.LibrarySessionUiState
import com.chitalka.navigation.DrawerScreen
import com.chitalka.screens.common.BookListSearchFilter
import com.chitalka.storage.StorageService
import com.chitalka.theme.ThemeMode
import com.chitalka.ui.topbar.AppTopBarSpec
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
internal fun ChitalkaMainShell(
    controller: ChitalkaAppController,
    librarySession: LibrarySessionState,
    sessionState: LibrarySessionUiState,
    storage: StorageService,
    persistence: LastOpenBookPersistence,
    locale: AppLocale,
    themeMode: ThemeMode,
    listRefreshNonce: Int,
    importSessionEpoch: Int,
    selected: DrawerScreen,
    onSelectedChange: (DrawerScreen) -> Unit,
    onRequestImport: () -> Unit,
    onLocaleChange: (AppLocale) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val normalizedSearchQuery =
        remember(searchQuery) {
            BookListSearchFilter.normalizeBookListSearchQuery(searchQuery)
        }

    LaunchedEffect(importSessionEpoch) {
        if (importSessionEpoch > 0) {
            librarySession.setSuppressWelcomeForPicker(false)
        }
    }

    LaunchedEffect(selected) {
        if (AppTopBarSpec.shouldAutoCloseSearchForRoute(selected.routeName, isSearchOpen)) {
            isSearchOpen = false
            searchQuery = ""
        }
    }

    val searchChrome =
        remember(sessionState.bookCount, isSearchOpen, searchQuery) {
            AppTopBarSpec.SearchChromeState(
                bookCount = sessionState.bookCount,
                isSearchOpen = isSearchOpen,
                searchQuery = searchQuery,
            )
        }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChitalkaDrawerContent(
                selected = selected,
                onSelect = { screen ->
                    onSelectedChange(screen)
                    scope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Scaffold(
            modifier = Modifier.testTag("chitalka_root"),
            topBar = {
                ChitalkaTopBar(
                    selected = selected,
                    searchChrome = searchChrome,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onOpenDrawer = {
                        // Поле поиска удерживает фокус и IME поверх drawer'а — первый
                        // тап по пункту уходит на dismiss клавиатуры, а не на onClick.
                        // Снимаем фокус и закрываем поиск до старта анимации drawer'а.
                        keyboardController?.hide()
                        focusManager.clearFocus(force = true)
                        if (isSearchOpen) {
                            isSearchOpen = false
                            searchQuery = ""
                        }
                        scope.launch { drawerState.open() }
                    },
                    onOpenSearch = { isSearchOpen = true },
                    onCloseSearch = {
                        isSearchOpen = false
                        searchQuery = ""
                    },
                    onClearQuery = { searchQuery = "" },
                )
            },
        ) { padding ->
            ShellContent(
                padding = padding,
                selected = selected,
                controller = controller,
                librarySession = librarySession,
                storage = storage,
                persistence = persistence,
                locale = locale,
                themeMode = themeMode,
                listRefreshNonce = listRefreshNonce,
                normalizedSearchQuery = normalizedSearchQuery,
                onRequestImport = onRequestImport,
                onLocaleChange = onLocaleChange,
                onThemeModeChange = onThemeModeChange,
            )
        }
    }

    if (sessionState.isFirstLaunchWelcomeVisible) {
        WelcomeDialog(
            welcomePickerHint = sessionState.welcomePickerHint,
            onPick = {
                librarySession.setSuppressWelcomeForPicker(true)
                onRequestImport()
            },
            onDismiss = {
                librarySession.setWelcomePickerHint(null)
                librarySession.dismissWelcomeModal()
            },
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun ShellContent(
    padding: PaddingValues,
    selected: DrawerScreen,
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
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        ChitalkaDrawerRouter(
            screen = selected,
            controller = controller,
            librarySession = librarySession,
            storage = storage,
            persistence = persistence,
            locale = locale,
            themeMode = themeMode,
            listRefreshNonce = listRefreshNonce,
            normalizedSearchQuery = normalizedSearchQuery,
            onRequestImport = onRequestImport,
            onLocaleChange = onLocaleChange,
            onThemeModeChange = onThemeModeChange,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
