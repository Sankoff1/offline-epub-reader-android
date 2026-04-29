@file:Suppress("LongMethod")

package com.ncorti.kotlin.template.app.ui

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.chitalka.debug.ChitalkaMirrorLog
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.loadPersistedLocale
import com.chitalka.library.BookFallbackLabels
import com.chitalka.library.LibraryImportSpec
import com.chitalka.library.importEpubToLibrary
import com.chitalka.library.refreshBookCount
import com.chitalka.library.restoreLastOpenReaderIfNeeded
import com.chitalka.navigation.DrawerScreen
import com.chitalka.navigation.RootStackRoutes
import com.chitalka.picker.EpubPickResult
import com.chitalka.picker.EpubPickerAndroid
import com.chitalka.theme.ThemeMode
import com.chitalka.theme.ThemeUiState
import com.chitalka.theme.loadPersistedThemeMode
import com.ncorti.kotlin.template.app.AppContainer
import com.ncorti.kotlin.template.app.ui.i18n.applyAppLocaleToSystem
import com.ncorti.kotlin.template.app.ui.i18n.chitalkaString
import com.ncorti.kotlin.template.app.ui.theme.ChitalkaMaterialTheme
import kotlinx.coroutines.launch

@Composable
fun ChitalkaApp(activity: ComponentActivity, container: AppContainer) {
    val persistence = container.persistence
    val storage = container.storage
    val librarySession = container.librarySession
    val navController = rememberNavController()

    var themeMode by remember { mutableStateOf(ThemeMode.LIGHT) }
    var locale by remember { mutableStateOf(AppLocale.RU) }
    val listNonce = remember { mutableIntStateOf(0) }
    var importSessionEpoch by remember { mutableIntStateOf(0) }
    var selectedDrawer by remember { mutableStateOf(DrawerScreen.ReadingNow) }

    val sessionState by librarySession.state.collectAsStateWithLifecycle()

    val readerCoordinator = rememberReaderNavCoordinator(activity, navController)
    ReaderNavCoordinatorSideEffects(navController, readerCoordinator)

    val controller = remember(readerCoordinator) {
        ChitalkaAppController(readerCoordinator) { listNonce.intValue++ }
    }

    LaunchedEffect(persistence) {
        themeMode = loadPersistedThemeMode(persistence) ?: ThemeMode.LIGHT
        locale = loadPersistedLocale(persistence) ?: AppLocale.RU
        applyAppLocaleToSystem(activity, locale)
    }

    LaunchedEffect(storage, persistence, controller) {
        librarySession.refreshBookCount(storage)
        librarySession.markStorageReady(true)
        // Activity recreate (смена темы/локали системой) пересоздаёт композицию, но NavController
        // переживает recreate через rememberNavController. Если читалка уже на стеке — повторное
        // восстановление положит её сверху и пользователю придётся жать Back несколько раз.
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute?.startsWith("${RootStackRoutes.READER}/") == true) {
            return@LaunchedEffect
        }
        restoreLastOpenReaderIfNeeded(
            lookup = storage,
            lastOpenPersistence = persistence,
            openReader = { fileUri, bookId -> controller.openReader(bookId, fileUri) },
        )
    }

    LaunchedEffect(storage, listNonce.intValue) {
        if (listNonce.intValue > 0) {
            librarySession.refreshBookCount(storage)
        }
    }

    val importLauncher =
        rememberLauncherForActivityResult(EpubPickerAndroid.openDocumentContract()) { uri ->
            importSessionEpoch++
            activity.lifecycleScope.launch {
                val pick =
                    try {
                        EpubPickerAndroid.mapOpenDocumentUri(uri, activity.contentResolver)
                    } catch (e: Exception) {
                        ChitalkaMirrorLog.w("ChitalkaApp", "mapOpenDocumentUri failed uri=$uri", e)
                        EpubPickerAndroid.pickFailedResult()
                    }
                librarySession.setSuppressWelcomeForPicker(false)
                when (pick) {
                    is EpubPickResult.Ok -> {
                        try {
                            val fallbackLabels =
                                BookFallbackLabels(
                                    untitled = chitalkaString(
                                        activity, locale, LibraryImportSpec.I18nKeys.BOOK_UNTITLED,
                                    ),
                                    unknownAuthor = chitalkaString(
                                        activity, locale, LibraryImportSpec.I18nKeys.BOOK_UNKNOWN_AUTHOR,
                                    ),
                                )
                            importEpubToLibrary(
                                context = activity,
                                sourceUri = pick.uri,
                                bookId = pick.bookId,
                                storage = storage,
                                fallbackLabels = fallbackLabels,
                            )
                            librarySession.setWelcomePickerHint(null)
                            listNonce.intValue++
                            selectedDrawer = DrawerScreen.BooksAndDocs
                        } catch (e: Exception) {
                            ChitalkaMirrorLog.e(
                                "ChitalkaApp", "importEpubToLibrary failed bookId=${pick.bookId}", e,
                            )
                            librarySession.setWelcomePickerHint(LibraryImportSpec.I18nKeys.IMPORT_FAILED)
                        }
                    }
                    is EpubPickResult.Error -> {
                        librarySession.setWelcomePickerHint(pick.messageKey)
                    }
                    EpubPickResult.Canceled -> {
                    }
                }
            }
        }

    val themeUi = remember(themeMode) { ThemeUiState(mode = themeMode) }

    val view = LocalView.current
    val isDarkTheme = themeMode == ThemeMode.DARK
    SideEffect {
        val window = activity.window
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !isDarkTheme
            isAppearanceLightNavigationBars = !isDarkTheme
        }
        // Без этого OS подмешивает светлый scrim под жестовой панелью.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    ChitalkaMaterialTheme(
        mode = themeUi.mode,
        colors = themeUi.colors,
    ) {
        CompositionLocalProvider(
            LocalChitalkaLocale provides locale,
            LocalChitalkaThemeMode provides themeMode,
            LocalChitalkaThemeColors provides themeUi.colors,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .systemBarsPadding(),
            ) {
                ChitalkaNavHost(
                    navController = navController,
                    persistence = persistence,
                    librarySession = librarySession,
                    storage = storage,
                ) {
                    ChitalkaMainShell(
                        controller = controller,
                        librarySession = librarySession,
                        sessionState = sessionState,
                        storage = storage,
                        persistence = persistence,
                        locale = locale,
                        themeMode = themeMode,
                        listRefreshNonce = listNonce.intValue,
                        importSessionEpoch = importSessionEpoch,
                        selected = selectedDrawer,
                        onSelectedChange = { selectedDrawer = it },
                        onRequestImport = {
                            importLauncher.launch(EpubPickerAndroid.openDocumentMimeTypes())
                        },
                        onLocaleChange = { newLocale ->
                            locale = newLocale
                            applyAppLocaleToSystem(activity, newLocale)
                        },
                        onThemeModeChange = { themeMode = it },
                    )
                }
            }
        }
    }
}
